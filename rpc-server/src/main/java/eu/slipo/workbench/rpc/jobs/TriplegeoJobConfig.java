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
import java.util.Properties;
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
import eu.slipo.workbench.rpc.jobs.tasklet.PrepareWorkingDirectoryTasklet;
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
     * The default key for the (single) configuration file
     */
    public static final String CONFIG_KEY = "options";
    
    /**
     * The default filename for the (single) configuration file
     */
    public static final String CONFIG_FILENAME = "options.conf";
    
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
    
    private Properties buildConfigProperties(
        EnumDataFormat inputFormat, EnumDataFormat outputFormat, String featureName)
    {
        Properties p = new Properties();
        
        p.setProperty("inputFormat", inputFormat.name());
        p.setProperty("serialization", outputFormat.name());
        p.setProperty("featureName", featureName);
        
        // Populate with defaults
        
        p.setProperty("tmpDir", "/tmp");
        p.setProperty("targetOntology", "GeoSPARQL");
        p.setProperty("prefixFeatureNS", "georesource");
        p.setProperty("nsFeatureURI", "http://slipo.eu/geodata#");
        p.setProperty("prefixGeometryNS", "geo");
        p.setProperty("nsGeometryURI", "http://www.opengis.net/ont/geosparql#");
        
        p.setProperty("defaultLang", "en");
        
        // Populate with per-inputFormat defaults
        
        switch (inputFormat) {
        case CSV:
            p.setProperty("mode", "STREAM");
            p.setProperty("delimiter", "|");
            p.setProperty("attrKey", "id");
            p.setProperty("attrName", "name");
            p.setProperty("attrCategory", "type");
            p.setProperty("attrX", "lon");
            p.setProperty("attrY", "lat");
            p.setProperty("valIgnore", "UNK");
            break;
        case SHAPEFILE:
            p.setProperty("mode", "GRAPH");
            p.setProperty("attrKey", "id");
            p.setProperty("attrName", "name");
            p.setProperty("attrCategory", "type");
            p.setProperty("valIgnore", "UNK");
            break;
        case GEOJSON:
            p.setProperty("mode", "STREAM");
            p.setProperty("attrKey", "id");
            p.setProperty("attrName", "name");
            p.setProperty("attrCategory", "type");
            p.setProperty("valIgnore", "null");
            break;
        case GPX:
            p.setProperty("mode", "STREAM");
            p.setProperty("attrKey", "name");
            p.setProperty("attrName", "name");
            p.setProperty("valIgnore", "UNK");
            break;
        case OSM:
            break;
        default:
            break;
        }
        
        return p;
    }
    
    /**
     * Α tasklet to prepare the working directory for a Triplegeo job instance.
     */
    @Bean("triplegeo.prepareWorkingDirectoryTasklet")
    @JobScope
    public PrepareWorkingDirectoryTasklet prepareWorkingDirectoryTasklet(
        @Value("#{jobExecutionContext['inputFormat']}") String inputFormat,
        @Value("#{jobExecutionContext['outputFormat']}") String outputFormat,
        @Value("#{jobExecutionContext['input']}") String inputFiles,
        @Value("#{jobExecutionContext['featureName']}") String featureName,
        @Value("#{jobExecution.jobInstance.id}") Long jobId) 
    {
        Path workDir = dataDir.resolve(String.valueOf(jobId));
        
        EnumDataFormat inputDataFormat = EnumDataFormat.valueOf(inputFormat);
        EnumDataFormat outputDataFormat = EnumDataFormat.valueOf(outputFormat);
        
        Properties configProperties = 
            buildConfigProperties(inputDataFormat, outputDataFormat, featureName);
        
        return PrepareWorkingDirectoryTasklet.builder()
            .workingDirectory(workDir)
            .input(inputFiles.split(File.pathSeparator))
            .inputFormat(inputDataFormat)
            .config(CONFIG_KEY, CONFIG_FILENAME, configProperties)
            .build();
    }
    
    @Bean("triplegeo.prepareWorkingDirectoryStep")
    public Step prepareWorkingDirectoryStep(
        @Qualifier("triplegeo.prepareWorkingDirectoryTasklet") PrepareWorkingDirectoryTasklet tasklet) 
        throws Exception
    {
        StepExecutionListener stepContextListener = ExecutionContextPromotionListeners
            .fromKeys("workDir", "inputDir", "input", "outputDir", "config")
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
        public void beforeJob(JobExecution execution)
        {
            JobParameters parameters = execution.getJobParameters();
            ExecutionContext executionContext = execution.getExecutionContext();
            
            // Initialize execution-context from job parameters
            
            executionContext.putString("inputFormat", 
                parameters.getString("inputFormat", "SHAPEFILE"));
            
            executionContext.putString("outputFormat", 
                parameters.getString("outputFormat", "TURTLE"));
           
            executionContext.putString("input", parameters.getString("input"));
            
            executionContext.putString("featureName", parameters.getString("featureName"));
        }
        
        @Override
        public void afterJob(JobExecution execution)
        {
            JobInstance instance = execution.getJobInstance();
            logger.info("After job {}#{}: status={} exit-status={}", 
                instance.getJobName(), instance.getId(), 
                execution.getStatus(), execution.getExitStatus());
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
