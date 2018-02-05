package eu.slipo.workbench.rpc.jobs;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.JobFactory;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
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

import eu.slipo.workbench.common.model.ApplicationException;
import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.tool.EnumConfigurationFormat;
import eu.slipo.workbench.common.model.tool.InvalidConfigurationException;
import eu.slipo.workbench.common.model.tool.ToolConfigurationSupport;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;
import eu.slipo.workbench.common.service.tool.ConfigurationGeneratorService;
import eu.slipo.workbench.common.service.util.PropertiesConverterService;
import eu.slipo.workbench.rpc.jobs.listener.ExecutionContextPromotionListeners;
import eu.slipo.workbench.rpc.jobs.listener.LoggingJobExecutionListener;
import eu.slipo.workbench.rpc.jobs.tasklet.PrepareWorkingDirectoryTasklet;
import eu.slipo.workbench.rpc.jobs.tasklet.docker.CreateContainerTasklet;
import eu.slipo.workbench.rpc.jobs.tasklet.docker.RunContainerTasklet;

@Component
public class TriplegeoJobConfiguration
{
    private static final String JOB_NAME = "triplegeo";
    
    private static final Set<PosixFilePermission> DIRECTORY_PERMISSIONS = 
        PosixFilePermissions.fromString("rwxr-xr-x");
    
    private static final FileAttribute<?> DIRECTORY_ATTRIBUTE = 
        PosixFilePermissions.asFileAttribute(DIRECTORY_PERMISSIONS);
        
    /**
     * The default key for the (single) configuration file
     */
    public static final String CONFIG_KEY = "options";
    
    /**
     * The default filename for the (single) configuration file
     */
    public static final String CONFIG_FILENAME = "options.conf";
    
    /**
     * The default timeout (milliseconds) for a container run
     */
    public static final long DEFAULT_RUN_TIMEOUT = 30 * 1000L;
    
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
    private Path jobDataDirectory;
    
    /**
     * The root directory on the docker host, under which we bind-mount volumes.
     */
    private Path dataDir;
    
    /**
     * The root directory on a container, under which directories (or files) will be 
     * mounted from the host.
     * <p>This directory is typically be under the <tt>/var</tt> or <tt>/var/local</tt> 
     * hierarchy.
     */
    private Path containerDataDir;
    
    @Autowired
    private void setContainerDataDirectory(
        @Value("${slipo.rpc-server.tools.triplegeo.docker.container-data-dir:/var/local/triplegeo}") String dir)
    {
        Assert.notNull(dir, "Expected a non-null directory path");
        Path path = Paths.get(dir);
        Assert.isTrue(path.isAbsolute(), "Expected an absolute path (inside a container)");
        this.containerDataDir = path;
    }
    
    @PostConstruct
    private void setupDataDirectory() throws IOException
    {
        this.dataDir = jobDataDirectory.resolve("triplegeo");
        try {
            Files.createDirectory(dataDir, DIRECTORY_ATTRIBUTE); 
        } catch (FileAlreadyExistsException e) {}
    }

    /**
     * A tasklet to map job parameters into step execution-context.
     */
    @Bean("triplegeo.setupExecutionContextTasklet")
    public Tasklet setupExecutionContextTasklet()
    {
        return new Tasklet()
        {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext context)
                throws Exception
            {
                StepContext stepContext = context.getStepContext();
                ExecutionContext executionContext = stepContext.getStepExecution().getExecutionContext();
                
                TriplegeoConfiguration config = propertiesConverterService.propertiesToValue(
                    stepContext.getJobParameters(), TriplegeoConfiguration.class);
                
                executionContext.put("config", config);
                return RepeatStatus.FINISHED;
            }
        };
    }
    
    @Bean("triplegeo.setupExecutionContextStep")
    public Step setupExecutionContextStep(
        @Qualifier("triplegeo.setupExecutionContextTasklet") Tasklet tasklet)
    {
        StepExecutionListener stepContextListener = ExecutionContextPromotionListeners
            .fromKeys("config")
            .strict(true)
            .build();
        
        return stepBuilderFactory.get("triplegeo.setupExecutionContext")
            .tasklet(tasklet)
            .listener(stepContextListener)
            .build();
    }
    
    @Bean("triplegeo.validateConfigurationTasklet")
    @JobScope
    public Tasklet validateConfigurationTasklet(
        @Qualifier("beanValidator") Validator validator,
        @Value("#{jobExecutionContext['config']}") TriplegeoConfiguration config)
    {
        return new Tasklet()
        {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext context)
                throws Exception
            {
                Set<ConstraintViolation<TriplegeoConfiguration>> errors = 
                    validator.validate(config);
                if (!errors.isEmpty()) {
                    throw InvalidConfigurationException.fromErrors(errors);
                }
                return RepeatStatus.FINISHED;
            }
        };
    }
    
    @Bean("triplegeo.validateConfigurationStep")
    public Step validateConfigurationStep(
        @Qualifier("triplegeo.validateConfigurationTasklet") Tasklet tasklet)
    {
        return stepBuilderFactory.get("triplegeo.validateConfiguration")
            .tasklet(tasklet)
            .build();
    }
    
    /**
     * Α tasklet to prepare the working directory for a Triplegeo job instance.
     */
    @Bean("triplegeo.prepareWorkingDirectoryTasklet")
    @JobScope
    public PrepareWorkingDirectoryTasklet prepareWorkingDirectoryTasklet(
        ConfigurationGeneratorService configurationGeneratorService,
        @Value("#{jobExecutionContext['config']}") TriplegeoConfiguration config,
        @Value("#{jobExecution.jobInstance.id}") Long jobId) 
    {
        Path workDir = dataDir.resolve(String.format("%04x", jobId));
        
        return PrepareWorkingDirectoryTasklet.builder()
            .workingDirectory(workDir)
            .input(config.getInput())
            .inputFormat(config.getInputFormat())
            .configurationGeneratorService(configurationGeneratorService)
            .config(CONFIG_KEY, CONFIG_FILENAME, config, EnumConfigurationFormat.PROPERTIES)
            .build();
    }
    
    @Bean("triplegeo.prepareWorkingDirectoryStep")
    public Step prepareWorkingDirectoryStep(
        @Qualifier("triplegeo.prepareWorkingDirectoryTasklet") PrepareWorkingDirectoryTasklet tasklet) 
        throws Exception
    {
        StepExecutionListener stepContextListener = ExecutionContextPromotionListeners
            .fromKeys(
                "workDir", "inputDir", "inputFiles", "inputFormat", "outputDir", "configByName")
            .strict(true)
            .build();
        
        return stepBuilderFactory.get("triplegeo.prepareWorkingDirectory")
            .tasklet(tasklet)
            .listener(stepContextListener)
            .build();   
    }    
    
    @Bean("triplegeo.createContainerTasklet")
    @JobScope
    public CreateContainerTasklet createContainerTasklet(
        @Value("${slipo.rpc-server.tools.triplegeo.docker.image}") String imageName,
        @Value("#{jobExecution.jobInstance.id}") Long jobId,
        @Value("#{jobExecutionContext['workDir']}") String workDir, 
        @Value("#{jobExecutionContext['inputDir']}") String inputDir,
        @Value("#{jobExecutionContext['inputFormat']}") String inputFormatName,
        @Value("#{jobExecutionContext['inputFiles']}") List<String> inputFiles,
        @Value("#{jobExecutionContext['outputDir']}") String outputDir,
        @Value("#{jobExecutionContext['configByName']}") Map<String, String> configByName)
    {
        String containerName = String.format("triplegeo-%04x", jobId);
        
        Path containerInputDir = containerDataDir.resolve("input");
        Path containerOutputDir = containerDataDir.resolve("output");
        Path containerConfigDir = containerDataDir;
        
        EnumDataFormat inputFormat = EnumDataFormat.valueOf(inputFormatName);
        String inputNameExtension = inputFormat.getFilenameExtension();
       
        String input = inputFiles.stream()
            .filter(name -> StringUtils.getFilenameExtension(name).equals(inputNameExtension))
            .map(name -> containerInputDir.resolve(name).toString())
            .collect(Collectors.joining(File.pathSeparator));
        
        Path configPath = Paths.get(workDir).resolve(configByName.get(CONFIG_KEY));
        
        return CreateContainerTasklet.builder()
            .client(docker)
            .name(containerName)
            .container(configurer -> configurer
                .image(imageName)
                .volume(Paths.get(inputDir), containerInputDir)
                .volume(Paths.get(outputDir), containerOutputDir)
                .volume(configPath, containerConfigDir.resolve(CONFIG_FILENAME), true)
                .env("INPUT_FILE", input)
                .env("CONFIG_FILE", containerConfigDir.resolve(CONFIG_FILENAME).toString())
                .env("OUTPUT_DIR", containerOutputDir.toString()))
            .build();
    }
    
    @Bean("triplegeo.createContainerStep")
    public Step createContainerStep(
        @Qualifier("triplegeo.createContainerTasklet") CreateContainerTasklet tasklet) 
        throws Exception
    {
        StepExecutionListener stepContextListener = ExecutionContextPromotionListeners
            .fromKeys("containerId", "containerName")
            .build();
        
        return stepBuilderFactory.get("triplegeo.createContainer")
            .tasklet(tasklet)
            .listener(stepContextListener)
            .build();   
    }
    
    @Bean("triplegeo.runContainerTasklet")
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
    
    @Bean("triplegeo.runContainerStep")
    public Step runContainerStep(
        @Qualifier("triplegeo.runContainerTasklet") RunContainerTasklet tasklet) 
        throws Exception
    {       
        return stepBuilderFactory.get("triplegeo.runContainer")
            .tasklet(tasklet)
            .listener(tasklet)
            .build();
    }
    
    /**
     * Create the basic flow of a job, expecting relevant configuration as an instance
     * of {@link TriplegeoConfiguration} keyed under <tt>config</tt> inside execution context.
     */
    @Bean("triplegeo.basicFlow")
    public Flow basicFlow(
        @Qualifier("breakpointStep") Step breakpointStep, // Fixme breakpointStep
        @Qualifier("triplegeo.validateConfigurationStep") Step validateConfigurationStep,
        @Qualifier("triplegeo.prepareWorkingDirectoryStep") Step prepareWorkingDirectoryStep,
        @Qualifier("triplegeo.createContainerStep") Step createContainerStep, 
        @Qualifier("triplegeo.runContainerStep") Step runContainerStep)
    {
        return new FlowBuilder<Flow>("triplegeo.basicFlow")
            .start(validateConfigurationStep)
            .next(prepareWorkingDirectoryStep)
            .next(createContainerStep)
            //.next(breakpointStep) // Fixme breakpointStep
            .next(runContainerStep)
            .end();
    }
    
    /**
     * Create flow for a job expecting configuration via normal {@link JobParameters}.  
     */
    @Bean("triplegeo.flow")
    public Flow flow(
        @Qualifier("triplegeo.setupExecutionContextStep") Step setupExecutionContextStep,
        @Qualifier("triplegeo.basicFlow") Flow basicFlow)
    {
        return new FlowBuilder<Flow>("triplegeo.flow")
            .start(setupExecutionContextStep)
            .next(basicFlow)
            .build();
    }
    
    @Bean("triplegeo.job")
    public Job job(
        @Qualifier("triplegeo.setupExecutionContextStep") Step setupExecutionContextStep,
        @Qualifier("triplegeo.flow") Flow flow)
    {
        return jobBuilderFactory.get(JOB_NAME)
            .incrementer(new RunIdIncrementer())
            .listener(new LoggingJobExecutionListener())
            .start(flow)
                .end()
            .build();
    }
    
    @Bean("triplegeo.jobFactory")
    public JobFactory jobFactory(@Qualifier("triplegeo.job") Job job)
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
