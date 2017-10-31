package eu.slipo.workbench.rpc.jobs;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
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
import org.springframework.batch.core.configuration.JobFactory;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
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
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.spotify.docker.client.DockerClient;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.rpc.jobs.listener.ExecutionContextPromotionListeners;
import eu.slipo.workbench.rpc.jobs.tasklet.docker.CreateContainerTasklet;
import eu.slipo.workbench.rpc.jobs.tasklet.docker.RunContainerTasklet;

@Component
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
            // The directory already exists
        }
    }
    
    private static class PrepareWorkingDirectoryTasklet implements Tasklet
    {
        private final Path workDir;
        
        private final Path inputDir;
        
        private final Path outputDir;
        
        private final List<Path> input;
        
        private final EnumDataFormat inputFormat;
        
        private PrepareWorkingDirectoryTasklet(Path workDir, List<Path> input, EnumDataFormat format)
        {
            this.workDir = workDir.toAbsolutePath();
            this.inputDir = this.workDir.resolve("input");
            this.outputDir = this.workDir.resolve("output");
            this.input = input;
            this.inputFormat = format;
        }

        public PrepareWorkingDirectoryTasklet(Path workDir, String[] input, EnumDataFormat format)
        {
            this(
                workDir, 
                Arrays.stream(input).map(Paths::get).collect(Collectors.toList()),
                format);
        }
        
        public PrepareWorkingDirectoryTasklet(Path workDir, Path[] input, EnumDataFormat format)
        {
            this(workDir, Arrays.asList(input), format);
        }
        
        public PrepareWorkingDirectoryTasklet(Path workDir, String input, EnumDataFormat format)
        {
            this(workDir, input.split(File.pathSeparator), format);
        }
        
        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext context)
            throws Exception
        {
            StepExecution stepExecution = context.getStepContext().getStepExecution();
            ExecutionContext executionContext = stepExecution.getExecutionContext();
            
            // Create working directory hierarchy (unique per job instance)
            
            Files.createDirectory(workDir, DIRECTORY_ATTRIBUTE);
            Files.createDirectory(inputDir, DIRECTORY_ATTRIBUTE);
            Files.createDirectory(outputDir, DIRECTORY_ATTRIBUTE);
            
            // Link to each input from inside input directory
            // Todo Filter (?) input by extension of specified data-format
            
            List<String> names = new ArrayList<>(input.size());
            for (Path inputPath: input) {
               String name = createLinkFromInputDirectory(inputPath);
               names.add(name);
            }
           
            // Todo Generate configuration file inside working directory
            
            // Update execution context
            
            executionContext.putString("workDir", workDir.toString());
            executionContext.putString("inputDir", inputDir.toString());
            executionContext.putString("outputDir", outputDir.toString());
            executionContext.putString("inputFormat", inputFormat.name());
            executionContext.putString("input", String.join(File.pathSeparator, names));
            executionContext.putString("configFile", "options.conf");
            
            return null;
        }

        /**
         * Link to the given input file from inside our input directory.
         * 
         * <p>
         * A shallow link will be created, i.e. there is no attempt to create a nested structure 
         * inside input directory. If a hard link cannot be created (because of file-system limitations),
         * we reside to a symbolic link. 
         * Note that in the later case (symbolic link), we assume that no input will be moved/deleted 
         * throughout the whole job execution! 
         * 
         * @param inputPath
         * @return the link name 
         * @throws IOException 
         */
        private String createLinkFromInputDirectory(Path inputPath) throws IOException
        {
            Path name = inputPath.getFileName();
            
            Path dst = inputDir.resolve(name);
            Path link = null;
            try {
                link = Files.createLink(dst, inputPath);
            } catch (FileSystemException e) {
                link = null;
            }
            
            if (link == null) {
                link = Files.createSymbolicLink(dst, inputPath);
            }
            
            return name.toString();
        }
    }
    
    /**
     * Α tasklet to prepare the working directory for a job instance.
     */
    @Bean("triplegeo.prepareWorkingDirectoryTasklet")
    @JobScope
    public PrepareWorkingDirectoryTasklet prepareWorkingDirectoryTasklet(
        @Value("#{jobParameters['inputFormat']}") String inputFormat,
        @Value("#{jobParameters['input']}") String input,
        @Value("#{jobExecution.jobInstance.id}") Long jobId) 
    {
        Path workDir = dataDir.resolve(String.valueOf(jobId));
        
        return new PrepareWorkingDirectoryTasklet(
            workDir, input, EnumDataFormat.valueOf(inputFormat));
    }
    
    @Bean("triplegeo.prepareWorkingDirectoryStep")
    public Step prepareWorkingDirectoryStep(
        @Qualifier("triplegeo.prepareWorkingDirectoryTasklet") Tasklet tasklet) 
        throws Exception
    {
        StepExecutionListener stepContextListener = ExecutionContextPromotionListeners
            .fromKeys("workDir", "input", "inputFormat", "inputDir", "outputDir", "config")
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
        @Value("#{jobExecutionContext['workDir']}") String workDir)
    {
        final String containerName = String.format("triplegeo-%d", jobId);
        
        return CreateContainerTasklet.builder()
            .client(docker)
            .name(containerName)
            .container(configurer -> configurer
                .image(imageName)
                // Fixme specify configuration file for triplegeo
                // Fixme specify actual input/output files
                //.volume(dataDir.resolve("echo/output"), containerDataDir.resolve("output"))
                //.volume(dataDir.resolve("echo/input"), containerDataDir.resolve("input"))
                // Todo Set environment for triplegeo
                .env("INPUT_FILE", "Baz"))
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
        final long defaultTimeout = 30 * 1000L; 
        
        return RunContainerTasklet.builder()
            .client(docker)
            .checkInterval(1000L)
            .timeout(defaultTimeout)
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
    
    @Bean("triplegeo.job")
    public Job job(
        @Qualifier("triplegeo.prepareWorkingDirectoryStep") Step prepareWorkingDirectoryStep,
        @Qualifier("triplegeo.createContainerStep") Step createContainerStep, 
        @Qualifier("triplegeo.runContainerStep") Step runContainerStep)
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
    
    @Bean("triplegeo.flow")
    public Flow flow(
        @Qualifier("triplegeo.prepareWorkingDirectoryStep") Step prepareWorkingDirectoryStep,
        @Qualifier("triplegeo.createContainerStep") Step createContainerStep, 
        @Qualifier("triplegeo.runContainerStep") Step runContainerStep)
    {
        return new FlowBuilder<Flow>("triplegeo.flow")
            .start(prepareWorkingDirectoryStep)
            .next(createContainerStep)
            .next(runContainerStep)
            .end();
    }
    
    @Bean("triplegeo.jobFactory")
    public JobFactory jobFactory(@Qualifier("triplegeo.job") Job job)
    {
        return new JobFactory()
        {
            @Override
            public String getJobName()
            {
                return "triplegeo";
            }
            
            @Override
            public Job createJob()
            {
                return job;
            }
        };
    }
}
