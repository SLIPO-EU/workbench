package eu.slipo.workbench.rpc.jobs;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
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

import com.spotify.docker.client.DockerClient;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.tool.DeerConfiguration;
import eu.slipo.workbench.common.model.tool.InvalidConfigurationException;
import eu.slipo.workbench.rpc.jobs.listener.ExecutionContextPromotionListeners;
import eu.slipo.workbench.rpc.jobs.listener.LoggingJobExecutionListener;
import eu.slipo.workbench.rpc.jobs.tasklet.PrepareWorkingDirectoryTasklet;
import eu.slipo.workbench.rpc.jobs.tasklet.docker.CreateContainerTasklet;
import eu.slipo.workbench.rpc.jobs.tasklet.docker.RunContainerTasklet;

@Component
public class DeerJobConfiguration extends BaseJobConfiguration
{
    private static final String JOB_NAME = "deer";

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
     * (eg. <tt>/var/local/deer</tt>).
     */
    private Path containerDataDir;

    @Autowired
    private void setContainerDataDirectory(
        @Value("${slipo.rpc-server.tools.deer.docker.container-data-dir}") String dir)
    {
        Path dirPath = Paths.get(dir);
        Assert.isTrue(dirPath.isAbsolute(), "Expected an absolute path (inside a container)");
        this.containerDataDir = dirPath;
    }

    @Autowired
    private void setTimeout(
        @Value("${slipo.rpc-server.tools.deer.timeout-seconds:}") Integer timeoutSeconds)
    {
        this.runTimeout = timeoutSeconds == null? DEFAULT_RUN_TIMEOUT : (timeoutSeconds.longValue() * 1000L);
    }

    @Autowired
    private void setCheckInterval(
        @Value("${slipo.rpc-server.tools.deer.check-interval-millis:}") Integer checkInterval)
    {
        this.checkInterval = checkInterval == null? DEFAULT_CHECK_INTERVAL : checkInterval.longValue();
    }

    @PostConstruct
    private void setupDataDirectory() throws IOException
    {
        super.setupDataDirectory("deer");
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

            DeerConfiguration configuration =
                propertiesConverter.propertiesToValue(parameters, DeerConfiguration.class);

            String inputPath = configuration.getInputPath();
            Assert.isTrue(!StringUtils.isEmpty(inputPath), "An input path is required");
            Assert.isTrue(Paths.get(inputPath).isAbsolute(), "The input is expected as an absolute path");
            configuration.clearInput();

            String specLocation = configuration.getSpec();
            Assert.isTrue(specLocation != null && specLocation.matches("^(file|classpath):.*"),
                "The configuration graph is expected as a file-based resource location");
            configuration.setSpec("spec.ttl"); // a dummy name

            // Validate

            Set<ConstraintViolation<DeerConfiguration>> errors = validator.validate(configuration);
            if (!errors.isEmpty()) {
                throw InvalidConfigurationException.fromErrors(errors);
            }

            // Update execution context

            executionContext.put("config", configuration);
            executionContext.put("spec", specLocation);
            executionContext.put("input", inputPath);

            return null;
        }
    }

    /**
     * A tasklet that reads job parameters to a configuration bean into execution-context.
     */
    @Bean("deer.configureTasklet")
    public Tasklet configureTasklet()
    {
        return new ConfigureTasklet();
    }

    @Bean("deer.configureStep")
    public Step configureStep(@Qualifier("deer.configureTasklet") Tasklet tasklet)
    {
        String[] keys = new String[] { "config", "spec", "input" };

        return stepBuilderFactory.get("deer.configure")
            .tasklet(tasklet)
            .listener(ExecutionContextPromotionListeners.fromKeys(keys))
            .build();
    }

    @Bean("deer.prepareWorkingDirectoryTasklet")
    @JobScope
    public PrepareWorkingDirectoryTasklet prepareWorkingDirectoryTasklet(
        @Value("#{jobExecutionContext['config']}") DeerConfiguration config,
        @Value("#{jobExecutionContext['input']}") String inputPath,
        @Value("#{jobExecutionContext['spec']}") Resource specResource,
        @Value("#{jobExecution.jobInstance.id}") Long jobId)
    {
        Assert.notNull(inputPath, "An input path is required");

        Path workDir = dataDir.resolve(String.format("%05x", jobId));

        return PrepareWorkingDirectoryTasklet.builder()
            .workingDirectory(workDir)
            .input(inputPath)
            .inputFormat(config.getInputFormat())
            .outputFormat(config.getOutputFormat())
            .config("config", "config.ttl", specResource)
            .build();
    }

    @Bean("deer.prepareWorkingDirectoryStep")
    public Step prepareWorkingDirectoryStep(
        @Qualifier("deer.prepareWorkingDirectoryTasklet") PrepareWorkingDirectoryTasklet tasklet)
        throws Exception
    {
        String[] keys = new String[] {
            "workDir", "inputDir", "inputFormat", "inputFiles", "outputDir", "outputFormat",
            "configFileByName"
        };
        return stepBuilderFactory.get("deer.prepareWorkingDirectory")
            .tasklet(tasklet)
            .listener(ExecutionContextPromotionListeners.fromKeys(keys))
            .build();
    }

    @Bean("deer.createContainerTasklet")
    @JobScope
    public CreateContainerTasklet createContainerTasklet(
        @Value("${slipo.rpc-server.tools.deer.docker.image}") String imageName,
        @Value("#{jobExecution.jobInstance.id}") Long jobId,
        @Value("#{jobExecutionContext['workDir']}") String workDir,
        @Value("#{jobExecutionContext['inputDir']}") String inputDir,
        @Value("#{jobExecutionContext['inputFormat']}") String inputFormatName,
        @Value("#{jobExecutionContext['inputFiles']}") List<String> inputFiles,
        @Value("#{jobExecutionContext['outputFormat']}") String outputFormatName,
        @Value("#{jobExecutionContext['outputDir']}") String outputDir,
        @Value("#{jobExecutionContext['configFileByName']}") Map<String, String> configFileByName)
    {
        String containerName = String.format("deer-%05x", jobId);

        Path containerInputDir = containerDataDir.resolve("input");
        Path containerOutputDir = containerDataDir.resolve("output");
        Path containerConfigDir = containerDataDir;

        Assert.isTrue(inputFiles.size() == 1, "Expected a single input file");
        Path inputFileName = Paths.get(inputFiles.get(0)).getFileName();
        Path configPath = Paths.get(workDir, configFileByName.get("config"));

        return CreateContainerTasklet.builder()
            .client(docker)
            .name(containerName)
            .container(configurer -> configurer
                .image(imageName)
                .volume(Paths.get(inputDir), containerInputDir, true)
                .volume(Paths.get(outputDir), containerOutputDir)
                .volume(configPath, containerConfigDir.resolve("config.ttl"), true)
                .env("INPUT_FILE", containerInputDir.resolve(inputFileName))
                .env("OUTPUT_FORMAT", outputFormatName)
                .env("OUTPUT_DIR", containerOutputDir)
                .env("CONFIG_FILE", containerConfigDir.resolve("config.ttl")))
            .build();
    }

    @Bean("deer.createContainerStep")
    public Step createContainerStep(
        @Qualifier("deer.createContainerTasklet") CreateContainerTasklet tasklet)
        throws Exception
    {
        String[] keys = new String[] { "containerId", "containerName" };

        return stepBuilderFactory.get("deer.createContainer")
            .tasklet(tasklet)
            .listener(ExecutionContextPromotionListeners.fromKeys(keys))
            .build();
    }

    @Bean("deer.runContainerTasklet")
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

    @Bean("deer.runContainerStep")
    public Step runContainerStep(@Qualifier("deer.runContainerTasklet") RunContainerTasklet tasklet)
        throws Exception
    {
        return stepBuilderFactory.get("deer.runContainer")
            .tasklet(tasklet)
            .listener(tasklet)
            .build();
    }

    /**
     * Create flow for a job expecting and reading configuration via normal {@link JobParameters}.
     */
    @Bean("deer.flow")
    public Flow flow(
        @Qualifier("deer.configureStep") Step configureStep,
        @Qualifier("deer.prepareWorkingDirectoryStep") Step prepareWorkingDirectoryStep,
        @Qualifier("deer.createContainerStep") Step createContainerStep,
        @Qualifier("deer.runContainerStep") Step runContainerStep)
    {
        return new FlowBuilder<Flow>("deer.flow")
            .start(configureStep)
            .next(prepareWorkingDirectoryStep)
            .next(createContainerStep)
            .next(runContainerStep)
            .build();
    }

    @Bean("deer.job")
    public Job job(@Qualifier("deer.flow") Flow flow)
    {
        return jobBuilderFactory.get(JOB_NAME)
            .incrementer(new RunIdIncrementer())
            .listener(new LoggingJobExecutionListener())
            .start(flow)
                .end()
            .build();
    }

    @Bean("deer.jobFactory")
    public JobFactory jobFactory(@Qualifier("deer.job") Job job)
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
