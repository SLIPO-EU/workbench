package eu.slipo.workbench.rpc.jobs;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.JobFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spotify.docker.client.DockerClient;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.tool.FagiConfiguration;
import eu.slipo.workbench.common.model.tool.InvalidConfigurationException;
import eu.slipo.workbench.rpc.jobs.listener.ExecutionContextPromotionListeners;
import eu.slipo.workbench.rpc.jobs.listener.LoggingJobExecutionListener;
import eu.slipo.workbench.rpc.jobs.tasklet.PrepareWorkingDirectoryTasklet;
import eu.slipo.workbench.rpc.jobs.tasklet.docker.CreateContainerTasklet;
import eu.slipo.workbench.rpc.jobs.tasklet.docker.RunContainerTasklet;

@Component
public class FagiJobConfiguration extends BaseJobConfiguration
{
    private static final String JOB_NAME = "fagi";

    /**
     * The default timeout (milliseconds) for a container run
     */
    public static final long DEFAULT_RUN_TIMEOUT = 60 * 1000L;

    /**
     * The default interval (milliseconds) for polling a container
     */
    public static final long DEFAULT_CHECK_INTERVAL = 1000L;

    /**
     * A list of keys of parameters that should be ignored (blacklisted) as conflicting with
     * <tt>input</tt> parameter.
     */
    private static final List<String> blacklistedParameterKeys =
        ImmutableList.of("left.path", "right.path", "links.path");

    @Autowired
    private DockerClient docker;

    private long runTimeout = -1L;

    private long checkInterval = -1L;

    /**
     * The root directory on a container, under which directories/files will be bind-mounted
     */
    private Path containerDataDir;

    @Autowired
    private void setContainerDataDirectory(
        @Value("${slipo.rpc-server.tools.fagi.docker.container-data-dir}") String dir)
    {
        Path dirPath = Paths.get(dir);
        Assert.isTrue(dirPath.isAbsolute(), "Expected an absolute path (inside a container)");
        this.containerDataDir = dirPath;
    }

    @Autowired
    private void setTimeout(
        @Value("${slipo.rpc-server.tools.fagi.timeout-seconds:}") Integer timeoutSeconds)
    {
        this.runTimeout = timeoutSeconds == null? DEFAULT_RUN_TIMEOUT : (timeoutSeconds.longValue() * 1000L);
    }

    @Autowired
    private void setCheckInterval(
        @Value("${slipo.rpc-server.tools.fagi.check-interval-millis:}") Integer checkInterval)
    {
        this.checkInterval = checkInterval == null? DEFAULT_CHECK_INTERVAL : checkInterval.longValue();
    }

    @PostConstruct
    private void setupDataDirectory() throws IOException
    {
        super.setupDataDirectory("fagi");
    }

    public class ConfigureTasklet implements Tasklet
    {
        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
            throws Exception
        {
            StepContext stepContext = chunkContext.getStepContext();
            ExecutionContext executionContext = stepContext.getStepExecution().getExecutionContext();
            Map<String, ?> parameters = stepContext.getJobParameters();

            // Read given parameters

            parameters = Maps.filterKeys(parameters, key -> !blacklistedParameterKeys.contains(key));

            FagiConfiguration configuration =
                propertiesConverter.propertiesToValue(parameters, FagiConfiguration.class);

            String rulesSpec = configuration.getRulesSpec();
            Assert.isTrue(rulesSpec != null && rulesSpec.matches("^(file|classpath):.*"),
                "The ruleset is expected as a file-based resource location");
            configuration.setRulesSpec("rules.xml"); // a dummy name

            String leftPath = configuration.getLeftPath();
            String rightPath = configuration.getRightPath();
            String linksPath = configuration.getLinksPath();

            List<String> inputPaths = Arrays.asList(leftPath, rightPath, linksPath);
            Assert.isTrue(!Iterables.any(inputPaths, StringUtils::isEmpty),
                "The input is expected as a triple (left, right, links) of paths");
            Assert.isTrue(Iterables.all(inputPaths, p -> Paths.get(p).isAbsolute()),
                "The input is expected as a list of absolute paths");

            configuration.clearInput();

            // Validate

            Set<ConstraintViolation<FagiConfiguration>> errors = validator.validate(configuration);
            if (!errors.isEmpty()) {
                throw InvalidConfigurationException.fromErrors(errors);
            }

            // Update execution context

            executionContext.put("spec", configuration);
            executionContext.putString("rules", rulesSpec);
            executionContext.put("input", inputPaths);

            return null;
        }
    }

    /**
     * A tasklet that reads job parameters to a configuration bean into execution-context.
     */
    @Bean("fagi.configureTasklet")
    public Tasklet configureTasklet()
    {
        return new ConfigureTasklet();
    }

    @Bean("fagi.configureStep")
    public Step configureStep(@Qualifier("fagi.configureTasklet") Tasklet tasklet)
    {
        String[] keys = new String[] { "spec", "rules", "input" };

        return stepBuilderFactory.get("fagi.configure")
            .tasklet(tasklet)
            .listener(ExecutionContextPromotionListeners.fromKeys(keys))
            .build();
    }

    @Bean("fagi.prepareWorkingDirectoryTasklet")
    @JobScope
    public PrepareWorkingDirectoryTasklet prepareWorkingDirectoryTasklet(
        @Value("#{jobExecutionContext['spec']}") FagiConfiguration spec,
        @Value("#{jobExecutionContext['input']}") List<String> input,
        @Value("#{jobExecutionContext['rules']}") Resource rulesResource,
        @Value("#{jobExecution.jobInstance.id}") Long jobId)
    {
        Path workDir = dataDir.resolve(String.format("%05x", jobId));

        return PrepareWorkingDirectoryTasklet.builder()
            .workingDirectory(workDir)
            .input(Lists.transform(input, Paths::get))
            .inputFormat(spec.getInputFormat())
            .outputFormat(spec.getOutputFormat())
            .config("rules", "rules.xml", rulesResource)
            .build();
    }

    @Bean("fagi.prepareWorkingDirectoryStep")
    public Step prepareWorkingDirectoryStep(
        @Qualifier("fagi.prepareWorkingDirectoryTasklet") PrepareWorkingDirectoryTasklet tasklet)
        throws Exception
    {
        String[] keys = new String[] {
            "workDir", "inputDir", "inputFiles", "inputFormat", "outputDir", "outputFormat",
            "configFileByName"
        };

        return stepBuilderFactory.get("fagi.prepareWorkingDirectory")
            .tasklet(tasklet)
            .listener(ExecutionContextPromotionListeners.fromKeys(keys))
            .build();
    }

    @Bean("fagi.createContainerTasklet")
    @JobScope
    public CreateContainerTasklet createContainerTasklet(
        @Value("${slipo.rpc-server.tools.fagi.docker.image}") String imageName,
        @Value("#{jobExecution.jobInstance.id}") Long jobId,
        @Value("#{jobExecutionContext['spec']}") FagiConfiguration spec,
        @Value("#{jobExecutionContext['workDir']}") String workDir,
        @Value("#{jobExecutionContext['inputDir']}") String inputDir,
        @Value("#{jobExecutionContext['inputFormat']}") String inputFormatName,
        @Value("#{jobExecutionContext['inputFiles']}") List<String> inputFiles,
        @Value("#{jobExecutionContext['outputDir']}") String outputDir,
        @Value("#{jobExecutionContext['configFileByName']}") Map<String, String> configFileByName)
    {
        String containerName = String.format("fagi-%05x", jobId);

        Path containerInputDir = containerDataDir.resolve("input");
        Path containerOutputDir = containerDataDir.resolve("output");
        Path containerConfigDir = containerDataDir;

        Assert.state(inputFiles != null && inputFiles.size() == 3,
            "The input is expected as a triple (left, right, links) of files");
        Path leftFileName = Paths.get(inputFiles.get(0)).getFileName();
        Path rightFileName = Paths.get(inputFiles.get(1)).getFileName();
        Path linksFileName = Paths.get(inputFiles.get(2)).getFileName();

        FagiConfiguration.Input leftSpec = spec.getLeft();
        FagiConfiguration.Input rightSpec = spec.getRight();
        FagiConfiguration.Links linksSpec = spec.getLinks();

        FagiConfiguration.Output targetSpec = spec.getTarget();
        Path fusedFileName = Paths.get(targetSpec.getFusedPath()).getFileName();
        Path remainingFileName = Paths.get(targetSpec.getRemainingPath()).getFileName();
        Path reviewFileName = Paths.get(targetSpec.getReviewPath()).getFileName();
        Path statsFileName = Paths.get(targetSpec.getStatsPath()).getFileName();

        return CreateContainerTasklet.builder()
            .client(docker)
            .name(containerName)
            .container(configurer -> configurer
                .image(imageName)
                .volume(Paths.get(inputDir), containerInputDir, true)
                .volume(Paths.get(outputDir), containerOutputDir)
                .volume(Paths.get(workDir, configFileByName.get("rules")),
                    containerConfigDir.resolve("rules.xml"), true)
                // Set environment
                .env("LOCALE", spec.getLang())
                .env("INPUT_FORMAT", spec.getInputFormatAsString())
                .env("OUTPUT_FORMAT", spec.getOutputFormatAsString())
                .env("SIMILARITY", spec.getSimilarityAsString())
                .env("RULES_FILE", containerConfigDir.resolve("rules.xml"))
                .env("LEFT_ID", leftSpec.getId())
                .env("LEFT_FILE", containerInputDir.resolve(leftFileName))
                .env("LEFT_DATE", Optional.ofNullable(leftSpec.getDate())
                    .map(LocalDate::toString).orElse(""))
                .env("RIGHT_ID", rightSpec.getId())
                .env("RIGHT_FILE", containerInputDir.resolve(rightFileName))
                .env("RIGHT_DATE", Optional.ofNullable(rightSpec.getDate())
                    .map(LocalDate::toString).orElse(""))
                .env("LINKS_ID", linksSpec.getId())
                .env("LINKS_FILE", containerInputDir.resolve(linksFileName))
                .env("TARGET_ID", targetSpec.getId())
                .env("TARGET_MODE", targetSpec.getModeAsString())
                .env("TARGET_FUSED_NAME",
                    StringUtils.stripFilenameExtension(fusedFileName.toString()))
                .env("TARGET_REMAINING_NAME",
                    StringUtils.stripFilenameExtension(remainingFileName.toString()))
                .env("TARGET_REVIEW_NAME",
                    StringUtils.stripFilenameExtension(reviewFileName.toString()))
                .env("TARGET_STATS_NAME",
                    StringUtils.stripFilenameExtension(statsFileName.toString()))
                .env("OUTPUT_DIR", containerOutputDir))
            .build();
    }

    @Bean("fagi.createContainerStep")
    public Step createContainerStep(
        @Qualifier("fagi.createContainerTasklet") CreateContainerTasklet tasklet)
        throws Exception
    {
        return stepBuilderFactory.get("fagi.createContainer")
            .tasklet(tasklet)
            .listener(ExecutionContextPromotionListeners.fromKeys("containerId", "containerName"))
            .build();
    }

    @Bean("fagi.runContainerTasklet")
    @JobScope
    public RunContainerTasklet runContainerTasklet(
        @Value("#{jobExecutionContext['containerName']}") String containerName)
    {
        return RunContainerTasklet.builder()
            .client(docker)
            .checkInterval(checkInterval)
            .timeout(runTimeout)
            .container(containerName)
            .removeOnFinished(false)
            .build();
    }

    @Bean("fagi.runContainerStep")
    public Step runContainerStep(@Qualifier("fagi.runContainerTasklet") RunContainerTasklet tasklet)
        throws Exception
    {
        return stepBuilderFactory.get("fagi.runContainer")
            .tasklet(tasklet)
            .listener(tasklet)
            .build();
    }

    /**
     * Create flow for a job expecting and reading configuration via normal {@link JobParameters}.
     */
    @Bean("fagi.flow")
    public Flow flow(
        @Qualifier("fagi.configureStep") Step configureStep,
        @Qualifier("fagi.prepareWorkingDirectoryStep") Step prepareWorkingDirectoryStep,
        @Qualifier("fagi.createContainerStep") Step createContainerStep,
        @Qualifier("fagi.runContainerStep") Step runContainerStep)
    {
        return new FlowBuilder<Flow>("fagi.flow")
            .start(configureStep)
            .next(prepareWorkingDirectoryStep)
            .next(createContainerStep)
            .next(runContainerStep)
            .build();
    }

    @Bean("fagi.job")
    public Job job(@Qualifier("fagi.flow") Flow flow)
    {
        return jobBuilderFactory.get(JOB_NAME)
            .incrementer(new RunIdIncrementer())
            .listener(new LoggingJobExecutionListener())
            .start(flow)
                .end()
            .build();
    }

    @Bean("fagi.jobFactory")
    public JobFactory jobFactory(@Qualifier("fagi.job") Job job)
    {
        return new JobFactory()
        {
            @Override
            public String getJobName()
            {
                return JOB_NAME;
            }

            @Override
            public Job createJob()
            {
                return job;
            }
        };
    }
}
