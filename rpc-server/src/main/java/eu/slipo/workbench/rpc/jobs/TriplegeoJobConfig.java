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
import java.util.Map;
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
import org.springframework.util.StringUtils;

import com.spotify.docker.client.DockerClient;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.rpc.jobs.listener.ExecutionContextPromotionListeners;
import eu.slipo.workbench.rpc.jobs.listener.LoggingJobExecutionListener;
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
     * The default timeout (milliseconds) for a container run
     */
    public static final long DEFAULT_RUN_TIMEOUT = 30 * 1000L;
    
    /**
     * The default interval (milliseconds) for polling a container 
     */
    public static final long DEFAULT_CHECK_INTERVAL = 1000L;
    
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
    
    /**
     * Map a data format ({@link EnumDataFormat}) to a name recognizable by Triplegeo tool.
     * <p>
     * Todo: This should be part of a separate tool configuration class
     * 
     * @param dataFormat
     */
    private String mapDataFormatToName(EnumDataFormat dataFormat)
    {
        String name = null;
        
        switch (dataFormat)
        {
        case CSV:
        case GPX:
        case SHAPEFILE:
        case GEOJSON:
        case N3:
        case TURTLE:
        case OSM:
            name = dataFormat.name();
            break;
        case N_TRIPLES:
            name = "N-TRIPLES";
            break;
        case RDF_XML:
            name = "RDF/XML";
            break;
        case RDF_XML_ABBREV:
            name = "RDF/XML-ABBREV";
            break;
        default:
            name = "";
            break;
        }
        return name;
    }

    /**
     * Build a map of properties as expected by Triplegeo tool
     * <p>
     * Todo: This should be part of a separate tool configuration class
     * 
     * @param inputFormat
     * @param outputFormat
     * @param executionContext The job execution-context
     * @return
     */
    private Properties buildConfigProperties(
        EnumDataFormat inputFormat, EnumDataFormat outputFormat, Map<String,Object> executionContext)
    {
        Properties p = new Properties();
        
        p.setProperty("inputFormat", mapDataFormatToName(inputFormat));
        p.setProperty("serialization", mapDataFormatToName(outputFormat));
        
        // Create placeholders for later values 
        
        p.setProperty("inputFiles", ""); 
        p.setProperty("outputDir", "");
        
        // Populate with defaults
        
        p.setProperty("featureName", 
            (String) executionContext.get("featureName"));
        
        p.setProperty("tmpDir", "/tmp");
        
        p.setProperty("targetOntology", 
            (String) executionContext.getOrDefault("targetOntology", "GeoSPARQL"));       
        
        p.setProperty("prefixFeatureNS", "georesource");
        p.setProperty("nsFeatureURI", "http://slipo.eu/geodata#");
        p.setProperty("prefixGeometryNS", "geo");
        p.setProperty("nsGeometryURI", "http://www.opengis.net/ont/geosparql#");
        p.setProperty("defaultLang", "en");
        
        p.setProperty("attrKey", 
            (String) executionContext.getOrDefault("attrKey", "id"));
        p.setProperty("attrName", 
            (String) executionContext.getOrDefault("attrName", "name"));
        p.setProperty("attrCategory", 
            (String) executionContext.getOrDefault("attrCategory", "type"));
        p.setProperty("valIgnore", 
            (String) executionContext.getOrDefault("valIgnore", "UNK"));
        
        // Populate or override with per-inputFormat defaults
        
        switch (inputFormat) {
        case CSV:
            p.setProperty("mode", "STREAM");
            p.setProperty("delimiter", 
                (String) executionContext.getOrDefault("delimiter", "|"));
            p.setProperty("attrX", 
                (String) executionContext.getOrDefault("attrX", "lon"));
            p.setProperty("attrY", 
                (String) executionContext.getOrDefault("attrY", "lat"));
            break;
        case SHAPEFILE:
            p.setProperty("mode", "GRAPH");
            break;
        case GEOJSON:
            p.setProperty("mode", "STREAM");
            break;
        case GPX:
            p.setProperty("mode", "STREAM");
            p.setProperty("attrKey", "name");
            p.setProperty("attrName", "name");
            break;
        default:
            break;
        }
        
        return p;
    }
    
    /**
     * A tasklet to map job parameters into step-wide execution context.
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
                Map<String,Object> parameters = stepContext.getJobParameters();
                
                executionContext.putString("inputFormat", 
                    (String) parameters.getOrDefault("inputFormat", "CSV"));
                
                executionContext.putString("outputFormat", 
                    (String) parameters.getOrDefault("outputFormat", "TURTLE"));
                
                executionContext.putString("input", (String) parameters.get("input"));
                
                executionContext.putString("featureName", 
                    (String) parameters.getOrDefault("featureName", "points"));
                
                return RepeatStatus.FINISHED;
            }
        };
    }
    
    @Bean("triplegeo.setupExecutionContextStep")
    public Step setupExecutionContextStep(
        @Qualifier("triplegeo.setupExecutionContextTasklet") Tasklet tasklet)
    {
        StepExecutionListener stepContextListener = ExecutionContextPromotionListeners
            .fromKeys("inputFormat", "input", "outputFormat", "featureName")
            .strict(true)
            .build();
        
        return stepBuilderFactory.get("triplegeo.setupExecutionContext")
            .tasklet(tasklet)
            .listener(stepContextListener)
            .build();
    }
    
    /**
     * Α tasklet to prepare the working directory for a Triplegeo job instance.
     */
    @Bean("triplegeo.prepareWorkingDirectoryTasklet")
    @JobScope
    public PrepareWorkingDirectoryTasklet prepareWorkingDirectoryTasklet(
        @Value("#{jobExecutionContext}") Map<String,Object> executionContext,
        @Value("#{jobExecution.jobInstance.id}") Long jobId) 
    {
        Path workDir = dataDir.resolve(String.format("%04x", jobId));
        
        String inputFormat = (String) executionContext.get("inputFormat");
        EnumDataFormat inputDataFormat = EnumDataFormat.valueOf(inputFormat);
        String outputFormat = (String) executionContext.get("outputFormat");
        EnumDataFormat outputDataFormat = EnumDataFormat.valueOf(outputFormat);
        
        String inputFiles = (String) executionContext.get("input");
        
        Properties configProperties = buildConfigProperties(
            inputDataFormat, outputDataFormat, executionContext);
        
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
        @Value("#{jobExecutionContext}") Map<String,Object> executionContext)
    {
        Path workDir = Paths.get((String) executionContext.get("workDir")); 
        Path inputDir = Paths.get((String) executionContext.get("inputDir"));
        Path outputDir = Paths.get((String) executionContext.get("outputDir"));
        
        Path containerInputDir = containerDataDir.resolve("input");
        Path containerOutputDir = containerDataDir.resolve("output");
        
        String inputFormat = (String) executionContext.get("inputFormat");
        EnumDataFormat inputDataFormat = EnumDataFormat.valueOf(inputFormat);
        String inputNameExtension = inputDataFormat.getFilenameExtension();
        
        List<?> inputNames = (List<?>) executionContext.get("input");
        String input = inputNames.stream()
            .map(name -> (String) name)
            .filter(name -> StringUtils.getFilenameExtension(name).equals(inputNameExtension))
            .map(name -> containerInputDir.resolve(name).toString())
            .collect(Collectors.joining(File.pathSeparator));
        
        Map<?,?> configNames = (Map<?,?>) executionContext.get("config");
        Path configPath = workDir.resolve((String) configNames.get(CONFIG_KEY));
        
        String containerName = String.format("triplegeo-%04x", jobId);
        
        return CreateContainerTasklet.builder()
            .client(docker)
            .name(containerName)
            .container(configurer -> configurer
                .image(imageName)
                .volume(inputDir, containerInputDir)
                .volume(outputDir, containerOutputDir)
                .volume(configPath, containerDataDir.resolve(CONFIG_FILENAME), true)
                .env("INPUT_FILE", input)
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
    
    private static class Validator implements JobParametersValidator
    {
        @Override
        public void validate(JobParameters parameters) throws JobParametersInvalidException
        {
            // Validate, raise exception on invalid parameters 
            
            String input = parameters.getString("input");
            if (StringUtils.isEmpty(input))
                throw new JobParametersInvalidException(
                    "The `input` parameter is missing (must contain a colon-separated list of inputs)");
            
            String inputFormat = parameters.getString("inputFormat", "");
            try {
                EnumDataFormat f = EnumDataFormat.valueOf(inputFormat);
            } catch (IllegalArgumentException e) {
                throw new JobParametersInvalidException(
                    "The `inputFormat` parameter is either missing or not recognized");
            }
            
            String outputFormat = parameters.getString("outputFormat", "");
            try {
                EnumDataFormat f = EnumDataFormat.valueOf(outputFormat);
            } catch (IllegalArgumentException e) {
                throw new JobParametersInvalidException(
                    "The `outputFormat` parameter is either missing or not recognized");
            }
            
            String featureName = parameters.getString("featureName");
            if (StringUtils.isEmpty(featureName))
                throw new JobParametersInvalidException(
                    "The `featureName` parameter is missing");
        }
    }
    
    @Bean("triplegeo.job")
    public Job job(
        @Qualifier("triplegeo.setupExecutionContextStep") Step setupExecutionContextStep,
        @Qualifier("triplegeo.prepareWorkingDirectoryStep") Step prepareWorkingDirectoryStep,
        @Qualifier("triplegeo.createContainerStep") Step createContainerStep, 
        @Qualifier("triplegeo.runContainerStep") Step runContainerStep)
    {
        return jobBuilderFactory.get(JOB_NAME)
            .incrementer(new RunIdIncrementer())
            .validator(new Validator())
            .listener(new LoggingJobExecutionListener())
            .start(setupExecutionContextStep)
            .next(prepareWorkingDirectoryStep)
            .next(createContainerStep)
            .next(runContainerStep)
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
