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
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

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
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;

import com.spotify.docker.client.DockerClient;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.rpc.jobs.listener.ExecutionContextPromotionListeners;
import eu.slipo.workbench.rpc.jobs.tasklet.docker.CreateContainerTasklet;
import eu.slipo.workbench.rpc.jobs.tasklet.docker.RunContainerTasklet;

public class TriplegeoJobConfig
{
    private static final String JOB_NAME = "triplegeo";
    
    private static final Set<PosixFilePermission> DIRECTORY_PERMISSIONS = 
        PosixFilePermissions.fromString("rwxr-xr-x");
    
    private static final FileAttribute<?> DIRECTORY_ATTRIBUTE = 
        PosixFilePermissions.asFileAttribute(DIRECTORY_PERMISSIONS);
    
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
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    
    @Autowired
    private DockerClient docker;
   
    @Autowired
    private void setDataDirectory(
        @Value("${slipo.rpc-server.docker.volumes.data-dir}") String dir)
    {
        Assert.notNull(dir, "Expected a non-null directory path");
        
        Path path = Paths.get(dir);
        Assert.isTrue(path.isAbsolute(), "Expected an absolute path");
        Assert.isTrue(Files.isDirectory(path) && Files.isWritable(path), 
            "Expected a path for a writable directory");
        
        this.dataDir = path.resolve("triplegeo");
    }
    
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
    private void initialize() throws IOException
    {
        // Create root data directory for our job executions
        
        try {
            Files.createDirectory(dataDir, DIRECTORY_ATTRIBUTE); 
        } catch (FileAlreadyExistsException e) {
            // no-op: The directory already exists
        }
    }
    
    private static class PrepareWorkingDirectoryTasklet implements Tasklet
    {
        private final Path workDir;
        
        private final List<Path> input;
        
        private final EnumDataFormat inputFormat;
        
        public PrepareWorkingDirectoryTasklet(Path workDir, List<Path> input, EnumDataFormat inputFormat)
        {
            this.workDir = workDir.toAbsolutePath();
            this.input = new ArrayList<>(input);
            this.inputFormat = inputFormat;
        }

        public PrepareWorkingDirectoryTasklet(Path workDir, String[] input, EnumDataFormat inputFormat)
        {
            this.workDir = workDir.toAbsolutePath();
            this.input = Arrays.stream(input)
                .map(s -> Paths.get(s))
                .collect(Collectors.toList());
            this.inputFormat = inputFormat;
        }
        
        public PrepareWorkingDirectoryTasklet(Path workDir, String input, EnumDataFormat inputFormat)
        {
            this(workDir, input.split(File.pathSeparator), inputFormat);
        }
        
        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext context)
            throws Exception
        {
            StepExecution stepExecution = context.getStepContext().getStepExecution();
            ExecutionContext executionContext = stepExecution.getExecutionContext();
            
            // Create working directory (unique per job instance)
            
            Files.createDirectory(workDir, DIRECTORY_ATTRIBUTE);
            
            // Todo Copy input inside work dir
            
            for (Path inputPath: input) {
                // ...
            }
              
            // Todo Relativize inputs into work dir
            
            // Todo set execution context
            
            executionContext.putString("workDir", workDir.toString());
            
            return null;
        }
        
    }
    
    /**
     * Α tasklet to prepare the working directory for a job instance.
     * 
     * @param inputFormat
     * @param input A colon-separated list of input files
     * @param jobId
     * @return
     */
    @Bean
    @JobScope
    public Tasklet prepareWorkingDirectoryTasklet(
        @Value("#{jobParameters['inputFormat']}") String inputFormat,
        @Value("#{jobParameters['input']}") String input,
        @Value("#{jobExecution.jobInstance.id}") Long jobId) 
    {
        Path workDir = dataDir.resolve(String.valueOf(jobId));
        
        return null; // Todo
    }
    
    @Bean
    public Step prepareWorkingDirectoryStep(
        @Qualifier("prepareWorkingDirectoryTasklet") Tasklet tasklet) 
        throws Exception
    {
        StepExecutionListener stepContextListener = ExecutionContextPromotionListeners
            .fromKeys("workDir", "input", "inputFormat")
            .strict(true)
            .build();
        
        return stepBuilderFactory.get("prepareWorkingDirectory")
            .tasklet(tasklet)
            .listener(stepContextListener)
            .build();   
    }    
    
    @Bean
    @JobScope
    public CreateContainerTasklet createTriplegeoContainerTasklet(
        @Value("${slipo.rpc-server.tools.triplegeo.docker.image:athenarc/triplegeo}") String imageName,
        @Value("#{jobExecution.id}") Long executionId) // Fixme not needed here
    {
        final String containerName = String.format("triplegeo-%d", executionId);
        
        Path dataDir = this.dataDir.resolve(String.valueOf(executionId)); 
        
        return CreateContainerTasklet.builder()
            .client(docker)
            .name(containerName)
            .container(configurer -> configurer
                .image(imageName)
                // Todo specify configuration file for triplegeo
                .volume(dataDir.resolve("echo/output"), containerDataDir.resolve("output"))
                .volume(dataDir.resolve("echo/input"), containerDataDir.resolve("input"))
                // Todo Set environment for triplegeo
                .env("Foo", "Baz"))
            .build();
    }
    
    @Bean
    public Step createTriplegeoContainerStep(
        @Qualifier("createTriplegeoContainerTasklet") CreateContainerTasklet tasklet) 
        throws Exception
    {
        StepExecutionListener stepContextListener = ExecutionContextPromotionListeners
            .fromKeys("containerId", "containerName")
            .build();
        
        return stepBuilderFactory.get("createTriplegeoContainer")
            .tasklet(tasklet)
            .listener(stepContextListener)
            .build();   
    }
    
    @Bean
    @JobScope
    public RunContainerTasklet runTriplegeoContainerTasklet(
        @Value("#{jobExecutionContext['containerName']}") String containerName)
    {
        final long defaultTimeout = 30 * 1000L; 
        
        return RunContainerTasklet.builder()
            .client(docker)
            .checkInterval(1000L)
            .timeout(defaultTimeout)
            .container(containerName)
            .removeOnFinished(false)
            .build();
    }
    
    @Bean
    public Step runTriplegeoContainerStep(
        @Qualifier("runTriplegeoContainerTasklet") RunContainerTasklet tasklet) 
        throws Exception
    {       
        return stepBuilderFactory.get("runTriplegeoContainer")
            .tasklet(tasklet)
            .listener(tasklet)
            .build();
    }
    
    private static class Validator implements JobParametersValidator
    {
        @Override
        public void validate(JobParameters parameters) throws JobParametersInvalidException
        {
            // Todo Validate, raise exception on invalid parameters 
        }
    }
    
    private static class ExecutionListener extends JobExecutionListenerSupport
    {
        private static Logger logger = LoggerFactory.getLogger(ExecutionListener.class); 

        @Override
        public void afterJob(JobExecution execution)
        {
            JobInstance r = execution.getJobInstance();
            logger.info("After {}#{}: status={} exit-status={}", 
                r.getJobName(), r.getId(), execution.getStatus(), execution.getExitStatus());
        }  
    }
    
    @Bean
    public Job job(
        Step prepareWorkingDirectoryStep,
        Step createTriplegeoContainerStep, 
        Step runTriplegeoContainerStep)
    {
        return jobBuilderFactory.get(JOB_NAME)
            .incrementer(new RunIdIncrementer())
            .validator(new Validator())
            .listener(new ExecutionListener())
            .start(prepareWorkingDirectoryStep)
            //.next(createTriplegeoContainerStep)
            //.next(runTriplegeoContainerStep)
            .build();
    }
}
