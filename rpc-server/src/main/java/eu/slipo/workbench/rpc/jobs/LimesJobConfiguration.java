package eu.slipo.workbench.rpc.jobs;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.validation.Validator;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobFactory;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.spotify.docker.client.DockerClient;

import eu.slipo.workbench.common.model.tool.EnumConfigurationFormat;
import eu.slipo.workbench.common.model.tool.LimesConfiguration;
import eu.slipo.workbench.common.service.util.PropertiesConverterService;
import eu.slipo.workbench.rpc.jobs.listener.ExecutionContextPromotionListeners;
import eu.slipo.workbench.rpc.jobs.listener.LoggingJobExecutionListener;
import eu.slipo.workbench.rpc.jobs.tasklet.PrepareWorkingDirectoryTasklet;
import eu.slipo.workbench.rpc.jobs.tasklet.ReadConfigurationTasklet;
import eu.slipo.workbench.rpc.jobs.tasklet.ValidateConfigurationTasklet;
import eu.slipo.workbench.rpc.jobs.tasklet.docker.CreateContainerTasklet;
import eu.slipo.workbench.rpc.jobs.tasklet.docker.RunContainerTasklet;
import eu.slipo.workbench.rpc.service.ConfigurationGeneratorService;

@Component
public class LimesJobConfiguration
{
    private static final String JOB_NAME = "limes";

    private static final FileAttribute<?> DIRECTORY_ATTRIBUTE =
        PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-xr-x"));

    /**
     * The default timeout (milliseconds) for a container run
     */
    public static final long DEFAULT_RUN_TIMEOUT = 60 * 1000L;

    /**
     * The default interval (milliseconds) for polling a container
     */
    public static final long DEFAULT_CHECK_INTERVAL = 1000L;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private DockerClient docker;

    @Autowired
    private PropertiesConverterService propertiesConverterService;

    @Autowired
    private ConfigurationGeneratorService configurationGeneratorService;

    @Autowired
    private Validator validator;

    @Autowired
    private Path jobDataDirectory;

    /**
     * The root directory on the docker host, under which we bind-mount volumes.
     */
    private Path dataDir;

    /**
     * The root directory on a container, under which directories/files will be bind-mounted
     * (eg. <tt>/var/local/foo</tt>).
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

    @PostConstruct
    private void setupDataDirectory() throws IOException
    {
        this.dataDir = jobDataDirectory.resolve("limes");
        try {
            Files.createDirectory(dataDir, DIRECTORY_ATTRIBUTE);
        } catch (FileAlreadyExistsException e) {}
    }

    /**
     * A tasklet that reads job parameters to a configuration bean into execution-context.
     */
    @Bean("limes.configureTasklet")
    public Tasklet configureTasklet()
    {
        return new ReadConfigurationTasklet<>(
            LimesConfiguration.class, propertiesConverterService, validator);
    }

    @Bean("limes.configureStep")
    public Step configureStep(@Qualifier("limes.configureTasklet") Tasklet tasklet)
    {
        return stepBuilderFactory.get("limes.configure")
            .tasklet(tasklet)
            .listener(ExecutionContextPromotionListeners.fromKeys("config"))
            .build();
    }

    @Bean("limes.prepareWorkingDirectoryTasklet")
    @JobScope
    public PrepareWorkingDirectoryTasklet prepareWorkingDirectoryTasklet(
        @Value("#{jobExecutionContext['config']}") LimesConfiguration config,
        @Value("#{jobExecution.jobInstance.id}") Long jobId)
    {
        Path workDir = dataDir.resolve(String.format("%05x", jobId));

        return PrepareWorkingDirectoryTasklet.builder()
            .workingDirectory(workDir)
            .input(config.getSourcePath(), config.getTargetPath())
            .inputFormat(config.getInputFormat())
            .configurationGeneratorService(configurationGeneratorService)
            .config("config", "config.xml", config, EnumConfigurationFormat.XML)
            .build();
    }

    @Bean("limes.prepareWorkingDirectoryStep")
    public Step prepareWorkingDirectoryStep(
        @Qualifier("limes.prepareWorkingDirectoryTasklet") PrepareWorkingDirectoryTasklet tasklet)
        throws Exception
    {
        String[] keys = new String[] {
            "workDir", "inputDir", "inputFormat", "outputDir", "configFileByName"
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
        @Value("#{jobExecutionContext['outputDir']}") String outputDir,
        @Value("#{jobExecutionContext['configFileByName']}") Map<String, String> configFileByName,
        @Value("#{jobExecutionContext['config']}") LimesConfiguration config)
    {
        String containerName = String.format("limes-%05x", jobId);

        Path containerInputDir = containerDataDir.resolve("input");
        Path containerOutputDir = containerDataDir.resolve("output");
        Path containerConfigDir = containerDataDir;

        Path sourceFileName = Paths.get(config.getSourcePath()).getFileName();
        Path targetFileName = Paths.get(config.getTargetPath()).getFileName();
        Path configPath = Paths.get(workDir, configFileByName.get("config"));

        return CreateContainerTasklet.builder()
            .client(docker)
            .name(containerName)
            .container(configurer -> configurer
                .image(imageName)
                .volume(Paths.get(inputDir), containerInputDir, true)
                .volume(Paths.get(outputDir), containerOutputDir)
                .volume(configPath, containerConfigDir.resolve("config.xml"), true)
                .env("SOURCE_FILE", containerInputDir.resolve(sourceFileName).toString())
                .env("TARGET_FILE", containerInputDir.resolve(targetFileName).toString())
                .env("CONFIG_FILE", containerConfigDir.resolve("config.xml").toString())
                .env("OUTPUT_DIR", containerOutputDir.toString()))
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
            .checkInterval(DEFAULT_CHECK_INTERVAL)
            .timeout(DEFAULT_RUN_TIMEOUT)
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
