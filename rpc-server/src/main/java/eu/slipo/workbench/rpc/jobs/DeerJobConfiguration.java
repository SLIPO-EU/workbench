package eu.slipo.workbench.rpc.jobs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.tool.DeerConfiguration;
import eu.slipo.workbench.common.model.tool.InvalidConfigurationException;
import eu.slipo.workbench.rpc.jobs.listener.ExecutionContextPromotionListeners;
import eu.slipo.workbench.rpc.jobs.listener.LoggingJobExecutionListener;
import eu.slipo.workbench.rpc.jobs.tasklet.PrepareWorkingDirectoryTasklet;
import eu.slipo.workbench.rpc.jobs.tasklet.docker.CreateContainerTasklet;
import eu.slipo.workbench.rpc.jobs.tasklet.docker.RunContainerTasklet;

@Component
public class DeerJobConfiguration extends ContainerBasedJobConfiguration
{
    private static final Logger logger = LoggerFactory.getLogger(DeerJobConfiguration.class);

    private static final String JOB_NAME = "deer";

    /**
     * The default timeout (milliseconds) for a container run
     */
    public static final long DEFAULT_RUN_TIMEOUT = 60 * 1000L;

    /**
     * The default interval (milliseconds) for polling a container
     */
    public static final long DEFAULT_CHECK_INTERVAL = 1000L;

    public static final long DEFAULT_MEMORY_LIMIT = 536870912L;

    @Override
    @Autowired
    protected void setContainerDataDirectory(
        @Value("${slipo.rpc-server.tools.deer.docker.container-data-dir}") String dir)
    {
        super.setContainerDataDirectory(dir);
    }

    @Autowired
    private void setImage(@Value("${slipo.rpc-server.tools.deer.docker.image}") String imageName)
    {
        this.imageName = imageName;
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

    @Autowired
    private void setMemoryLimit(
        @Value("${slipo.rpc-server.tools.deer.docker.container.memory-limit-kbytes:}") Long kbytes)
    {
        this.memoryLimit = kbytes == null? DEFAULT_MEMORY_LIMIT : (kbytes.longValue() * 1024L);
    }

    @Autowired
    private void setMemorySwapLimit(
        @Value("${slipo.rpc-server.tools.deer.docker.container.memoryswap-limit-kbytes:}") Long kbytes)
    {
        this.memorySwapLimit = kbytes == null? -1L : kbytes.longValue() * 1024;
    }

    @PostConstruct
    private void setMemoryLimitsIfNeeded()
    {
        if (this.memorySwapLimit < 0)
            this.memorySwapLimit = 2L * this.memoryLimit;

        logger.info("The memory limits are {}m/{}m",
            memoryLimit / 1024L / 1024L, memorySwapLimit / 1024L / 1024L);
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
            .listener(tasklet)
            .listener(ExecutionContextPromotionListeners.fromKeys(keys))
            .build();
    }

    @Bean("deer.createContainerTasklet")
    @JobScope
    public CreateContainerTasklet createContainerTasklet(
        @Value("#{jobExecution.jobInstance.id}") Long jobId,
        @Value("#{T(java.nio.file.Paths).get(jobExecutionContext['workDir'])}") Path workDir,
        @Value("#{T(java.nio.file.Paths).get(jobExecutionContext['inputDir'])}") Path inputDir,
        @Value("#{jobExecutionContext['inputFormat']}") String inputFormatName,
        @Value("#{jobExecutionContext['inputFiles']}") List<String> inputFiles,
        @Value("#{jobExecutionContext['outputFormat']}") String outputFormatName,
        @Value("#{T(java.nio.file.Paths).get(jobExecutionContext['outputDir'])}") Path outputDir,
        @Value("#{jobExecutionContext['configFileByName']}") Map<String, String> configFileByName)
            throws IOException
    {
        String containerName = String.format("deer-%05x", jobId);

        Assert.isTrue(inputFiles.size() == 1, "Expected a single input file");
        final String inputFileName = inputFiles.get(0);
        final String configFileName = configFileByName.get("config");

        final EnumDataFormat outputFormat = EnumDataFormat.valueOf(outputFormatName);
        final String resultFileName = "enriched" + "." + outputFormat.getFilenameExtension();
        final String statsFileName = "deer-analytics.json";

        // At container creation, we need to bind-mount the two (empty) output files: enrichment result and
        // analytics. The files need to exist (before container creation) in order to convince Docker to
        // mount them as files (and not as directories).
        Files.write(outputDir.resolve(resultFileName), new byte[0]);
        Files.write(outputDir.resolve(statsFileName), new byte[0]);

        // Create container
        return CreateContainerTasklet.builder()
            .client(docker)
            .name(containerName)
            .container(configurer -> configurer
                .image(imageName)
                .volume(inputDir.resolve(inputFileName), containerDataDir.resolve(Paths.get("input", inputFileName)), true)
                .volume(workDir.resolve(configFileName), containerDataDir.resolve("config.ttl"), true)
                .volume(outputDir.resolve(resultFileName), containerDataDir.resolve(Paths.get("output", resultFileName)))
                .volume(outputDir.resolve(statsFileName), containerDataDir.resolve(statsFileName))
                // Set environment
                .env("INPUT_FILE", containerDataDir.resolve(Paths.get("input", inputFileName)))
                .env("OUTPUT_FORMAT", outputFormatName)
                .env("OUTPUT_DIR", containerDataDir.resolve("output"))
                .env("OUTPUT_NAME", "enriched")
                .env("CONFIG_FILE", containerDataDir.resolve("config.ttl"))
                // Set resource limits
                .memory(memoryLimit)
                .memoryAndSwap(memorySwapLimit))
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
