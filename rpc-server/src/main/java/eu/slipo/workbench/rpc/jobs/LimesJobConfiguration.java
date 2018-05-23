package eu.slipo.workbench.rpc.jobs;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
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
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.spotify.docker.client.DockerClient;

import eu.slipo.workbench.common.model.tool.EnumConfigurationFormat;
import eu.slipo.workbench.common.model.tool.InvalidConfigurationException;
import eu.slipo.workbench.common.model.tool.LimesConfiguration;
import eu.slipo.workbench.rpc.jobs.listener.ExecutionContextPromotionListeners;
import eu.slipo.workbench.rpc.jobs.listener.LoggingJobExecutionListener;
import eu.slipo.workbench.rpc.jobs.tasklet.PrepareWorkingDirectoryTasklet;
import eu.slipo.workbench.rpc.jobs.tasklet.docker.CreateContainerTasklet;
import eu.slipo.workbench.rpc.jobs.tasklet.docker.RunContainerTasklet;

@Component
public class LimesJobConfiguration extends BaseJobConfiguration
{
    private static final String JOB_NAME = "limes";

    /**
     * The default timeout (milliseconds) for a container run
     */
    public static final long DEFAULT_RUN_TIMEOUT = 60 * 1000L;

    /**
     * The default interval (milliseconds) for polling a container
     */
    public static final long DEFAULT_CHECK_INTERVAL = 1000L;

    @Autowired
    private DockerClient docker;

    private long runTimeout = -1L;

    private long checkInterval = -1L;

    /**
     * The root directory on a container, under which directories/files will be bind-mounted
     * (eg. <tt>/var/local/limes</tt>).
     */
    private Path containerDataDir;

    @Autowired
    private void setContainerDataDirectory(
        @Value("${slipo.rpc-server.tools.limes.docker.container-data-dir}") String dir)
    {
        Path dirPath = Paths.get(dir);
        Assert.isTrue(dirPath.isAbsolute(), "Expected an absolute path (inside a container)");
        this.containerDataDir = dirPath;
    }

    @Autowired
    private void setTimeout(
        @Value("${slipo.rpc-server.tools.limes.timeout-seconds:}") Integer timeoutSeconds)
    {
        this.runTimeout = timeoutSeconds == null? DEFAULT_RUN_TIMEOUT : (timeoutSeconds.longValue() * 1000L);
    }

    @Autowired
    private void setCheckInterval(
        @Value("${slipo.rpc-server.tools.limes.check-interval-millis:}") Integer checkInterval)
    {
        this.checkInterval = checkInterval == null? DEFAULT_CHECK_INTERVAL : checkInterval.longValue();
    }

    @PostConstruct
    private void setupDataDirectory() throws IOException
    {
        super.setupDataDirectory("limes");
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

            LimesConfiguration config =
                propertiesConverter.propertiesToValue(parameters, LimesConfiguration.class);

            String sourcePath = config.getSourcePath();
            Assert.isTrue(!StringUtils.isEmpty(sourcePath), "A source path is required");
            Assert.isTrue(Paths.get(sourcePath).isAbsolute(), "The source is expected as an absolute path");

            String targetPath = config.getTargetPath();
            Assert.isTrue(!StringUtils.isEmpty(targetPath), "A target path is required");
            Assert.isTrue(Paths.get(targetPath).isAbsolute(), "The target is expected as an absolute path");

            config.clearInput();

            // Validate

            Set<ConstraintViolation<LimesConfiguration>> errors = validator.validate(config);
            if (!errors.isEmpty()) {
                throw InvalidConfigurationException.fromErrors(errors);
            }

            // Update execution context

            executionContext.put("config", config);
            executionContext.put("input", Arrays.asList(sourcePath, targetPath));

            return null;
        }
    }

    /**
     * A tasklet that reads job parameters to a configuration bean into execution-context.
     */
    @Bean("limes.configureTasklet")
    public Tasklet configureTasklet()
    {
        return new ConfigureTasklet();
    }

    @Bean("limes.configureStep")
    public Step configureStep(@Qualifier("limes.configureTasklet") Tasklet tasklet)
    {
        String[] keys = new String[] { "config", "input" };

        return stepBuilderFactory.get("limes.configure")
            .tasklet(tasklet)
            .listener(ExecutionContextPromotionListeners.fromKeys(keys))
            .build();
    }

    @Bean("limes.prepareWorkingDirectoryTasklet")
    @JobScope
    public PrepareWorkingDirectoryTasklet prepareWorkingDirectoryTasklet(
        @Value("#{jobExecutionContext['config']}") LimesConfiguration config,
        @Value("#{jobExecutionContext['input']}") List<String> inputPaths,
        @Value("#{jobExecution.jobInstance.id}") Long jobId)
    {
        Assert.notNull(inputPaths, "A list of input paths is required");
        Assert.isTrue(inputPaths.size() == 2, "Expected exactly 2 inputs to interlink");

        Path workDir = dataDir.resolve(String.format("%05x", jobId));
        String sourcePath = inputPaths.get(0);
        String targetPath = inputPaths.get(1);

        return PrepareWorkingDirectoryTasklet.builder()
            .workingDirectory(workDir)
            .input(sourcePath, targetPath)
            .inputFormat(config.getInputFormat())
            .outputFormat(config.getOutputFormat())
            .configurationGeneratorService(configurationGenerator)
            .config("config", "config.xml", config, EnumConfigurationFormat.XML)
            .build();
    }

    @Bean("limes.prepareWorkingDirectoryStep")
    public Step prepareWorkingDirectoryStep(
        @Qualifier("limes.prepareWorkingDirectoryTasklet") PrepareWorkingDirectoryTasklet tasklet)
        throws Exception
    {
        String[] keys = new String[] {
            "workDir", "inputDir", "inputFormat", "inputFiles", "outputDir", "outputFormat",
            "configFileByName"
        };
        return stepBuilderFactory.get("limes.prepareWorkingDirectory")
            .tasklet(tasklet)
            .listener(ExecutionContextPromotionListeners.fromKeys(keys))
            .build();
    }

    @Bean("limes.createContainerTasklet")
    @JobScope
    public CreateContainerTasklet createContainerTasklet(
        @Value("${slipo.rpc-server.tools.limes.docker.image}") String imageName,
        @Value("#{jobExecution.jobInstance.id}") Long jobId,
        @Value("#{jobExecutionContext['workDir']}") String workDir,
        @Value("#{jobExecutionContext['inputDir']}") String inputDir,
        @Value("#{jobExecutionContext['inputFormat']}") String inputFormatName,
        @Value("#{jobExecutionContext['inputFiles']}") List<String> inputFiles,
        @Value("#{jobExecutionContext['outputDir']}") String outputDir,
        @Value("#{jobExecutionContext['configFileByName']}") Map<String, String> configFileByName,
        @Value("#{jobExecutionContext['config']}") LimesConfiguration config)
    {
        String containerName = String.format("limes-%05x", jobId);

        Path containerInputDir = containerDataDir.resolve("input");
        Path containerOutputDir = containerDataDir.resolve("output");
        Path containerConfigDir = containerDataDir;

        Assert.isTrue(inputFiles.size() == 2, "Expected exactly 2 input files");
        Path sourceFileName = Paths.get(inputFiles.get(0)).getFileName();
        Path targetFileName = Paths.get(inputFiles.get(1)).getFileName();
        Path configPath = Paths.get(workDir, configFileByName.get("config"));

        String acceptedName = StringUtils.stripFilenameExtension(
            Paths.get(config.getAcceptedPath()).getFileName().toString());
        String reviewName = StringUtils.stripFilenameExtension(
            Paths.get(config.getReviewPath()).getFileName().toString());

        return CreateContainerTasklet.builder()
            .client(docker)
            .name(containerName)
            .container(configurer -> configurer
                .image(imageName)
                .volume(Paths.get(inputDir), containerInputDir, true)
                .volume(Paths.get(outputDir), containerOutputDir)
                .volume(configPath, containerConfigDir.resolve("config.xml"), true)
                .env("SOURCE_FILE", containerInputDir.resolve(sourceFileName))
                .env("TARGET_FILE", containerInputDir.resolve(targetFileName))
                .env("CONFIG_FILE", containerConfigDir.resolve("config.xml"))
                .env("OUTPUT_DIR", containerOutputDir)
                .env("ACCEPTED_NAME", acceptedName)
                .env("REVIEW_NAME", reviewName))
            .build();
    }

    @Bean("limes.createContainerStep")
    public Step createContainerStep(
        @Qualifier("limes.createContainerTasklet") CreateContainerTasklet tasklet)
        throws Exception
    {
        String[] keys = new String[] { "containerId", "containerName" };

        return stepBuilderFactory.get("limes.createContainer")
            .tasklet(tasklet)
            .listener(ExecutionContextPromotionListeners.fromKeys(keys))
            .build();
    }

    @Bean("limes.runContainerTasklet")
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

    @Bean("limes.runContainerStep")
    public Step runContainerStep(@Qualifier("limes.runContainerTasklet") RunContainerTasklet tasklet)
        throws Exception
    {
        return stepBuilderFactory.get("limes.runContainer")
            .tasklet(tasklet)
            .listener(tasklet)
            .build();
    }

    /**
     * Create flow for a job expecting and reading configuration via normal {@link JobParameters}.
     */
    @Bean("limes.flow")
    public Flow flow(
        @Qualifier("limes.configureStep") Step configureStep,
        @Qualifier("limes.prepareWorkingDirectoryStep") Step prepareWorkingDirectoryStep,
        @Qualifier("limes.createContainerStep") Step createContainerStep,
        @Qualifier("limes.runContainerStep") Step runContainerStep)
    {
        return new FlowBuilder<Flow>("limes.flow")
            .start(configureStep)
            .next(prepareWorkingDirectoryStep)
            .next(createContainerStep)
            .next(runContainerStep)
            .build();
    }

    @Bean("limes.job")
    public Job job(@Qualifier("limes.flow") Flow flow)
    {
        return jobBuilderFactory.get(JOB_NAME)
            .incrementer(new RunIdIncrementer())
            .listener(new LoggingJobExecutionListener())
            .start(flow)
                .end()
            .build();
    }

    @Bean("limes.jobFactory")
    public JobFactory jobFactory(@Qualifier("limes.job") Job job)
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
