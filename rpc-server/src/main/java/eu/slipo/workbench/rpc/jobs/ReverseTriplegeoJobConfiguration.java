package eu.slipo.workbench.rpc.jobs;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.tool.EnumConfigurationFormat;
import eu.slipo.workbench.common.model.tool.InvalidConfigurationException;
import eu.slipo.workbench.common.model.tool.ReverseTriplegeoConfiguration;
import eu.slipo.workbench.rpc.jobs.listener.ExecutionContextPromotionListeners;
import eu.slipo.workbench.rpc.jobs.listener.LoggingJobExecutionListener;
import eu.slipo.workbench.rpc.jobs.tasklet.PrepareWorkingDirectoryTasklet;
import eu.slipo.workbench.rpc.jobs.tasklet.docker.CreateContainerTasklet;
import eu.slipo.workbench.rpc.jobs.tasklet.docker.RunContainerTasklet;

@Component
public class ReverseTriplegeoJobConfiguration extends ContainerBasedJobConfiguration
{
    private static final Logger logger = LoggerFactory.getLogger(ReverseTriplegeoJobConfiguration.class);

    private static final String JOB_NAME = "reverseTriplegeo";

    /**
     * The default timeout (milliseconds) for a container run
     */
    public static final long DEFAULT_RUN_TIMEOUT = 60 * 1000L;

    /**
     * The default interval (milliseconds) for polling a container
     */
    public static final long DEFAULT_CHECK_INTERVAL = 1000L;

    public static final long DEFAULT_MEMORY_LIMIT = 268435456L;

    /**
     * A list of keys of parameters to be ignored (blacklisted) as conflicting with <tt>input</tt> parameter.
     */
    private static final List<String> blacklistedParameterKeys = ImmutableList.of("inputFiles");

    @Override
    @Autowired
    protected void setContainerDataDirectory(
        @Value("${slipo.rpc-server.tools.reverse-triplegeo.docker.container-data-dir}") String dir)
    {
        super.setContainerDataDirectory(dir);
    }

    @Autowired
    private void setTimeout(
        @Value("${slipo.rpc-server.tools.reverse-triplegeo.timeout-seconds:}") Integer timeoutSeconds)
    {
        this.runTimeout = timeoutSeconds == null? DEFAULT_RUN_TIMEOUT : (timeoutSeconds.longValue() * 1000L);
    }

    @Autowired
    private void setCheckInterval(
        @Value("${slipo.rpc-server.tools.reverse-triplegeo.check-interval-millis:}") Integer checkInterval)
    {
        this.checkInterval = checkInterval == null? DEFAULT_CHECK_INTERVAL : checkInterval.longValue();
    }

    @Autowired
    private void setMemoryLimit(
        @Value("${slipo.rpc-server.tools.reverse-triplegeo.docker.container.memory-limit-kbytes:}") Long kbytes)
    {
        this.memoryLimit = kbytes == null? DEFAULT_MEMORY_LIMIT : (kbytes.longValue() * 1024L);
    }

    @Autowired
    private void setMemorySwapLimit(
        @Value("${slipo.rpc-server.tools.reverse-triplegeo.docker.container.memoryswap-limit-kbytes:}") Long kbytes)
    {
        this.memorySwapLimit = kbytes == null? -1L : kbytes.longValue() * 1024L;
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
        super.setupDataDirectory("reverseTriplegeo");
    }

    public class ConfigureTasklet implements Tasklet
    {
        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
            throws Exception
        {
            StepContext stepContext = chunkContext.getStepContext();
            ExecutionContext executionContext = stepContext.getStepExecution().getExecutionContext();
            Map<String, Object> parameters = stepContext.getJobParameters();

            // Read given parameters

            parameters = Maps.filterKeys(parameters, key -> !blacklistedParameterKeys.contains(key));

            ReverseTriplegeoConfiguration configuration =
                propertiesConverter.propertiesToValue(parameters, ReverseTriplegeoConfiguration.class);

            String sparqlFileLocation = configuration.getSparqlFile();
            Assert.isTrue(sparqlFileLocation != null && sparqlFileLocation.matches("^(file|classpath):.*"),
                "The SPARQL query is expected as a file-based resource location");
            configuration.setSparqlFile("query.sparql"); // a dummy name

            List<String> inputPaths = new ArrayList<>(configuration.getInput());
            Assert.isTrue(!inputPaths.isEmpty() && !Iterables.any(inputPaths, StringUtils::isEmpty),
                "The input is expected as a non-empty list of paths");
            Assert.isTrue(Iterables.all(inputPaths, p -> Paths.get(p).isAbsolute()),
                "The input is expected as a list of absolute paths");
            configuration.clearInput();

            // Validate options

            Set<ConstraintViolation<ReverseTriplegeoConfiguration>> errors = validator.validate(configuration);
            if (!errors.isEmpty()) {
                throw InvalidConfigurationException.fromErrors(errors);
            }

            // Check if input/output formats are supported
            // The only supported input format is NT (N-TRIPLES): this is a limitation applied by workbench
            // (i.e. not by Triplegeo) because all normal Triplegeo operations result in N-TRIPLES.
            Assert.state(configuration.getInputFormat() == EnumDataFormat.N_TRIPLES,
                "The given input format is not supported (inside SLIPO workbench)");
            EnumDataFormat outputFormat = configuration.getOutputFormat();
            Assert.state(outputFormat == EnumDataFormat.CSV || outputFormat == EnumDataFormat.SHAPEFILE,
                "The given output format is not supported");

            // Update execution context

            executionContext.put("options", configuration);
            executionContext.putString("query", sparqlFileLocation);
            executionContext.put("input", inputPaths);

            return null;
        }
    }

    /**
     * A tasklet that reads job parameters to a configuration bean into execution-context.
     */
    @Bean("reverseTriplegeo.configureTasklet")
    public Tasklet configureTasklet()
    {
        return new ConfigureTasklet();
    }

    @Bean("reverseTriplegeo.configureStep")
    public Step configureStep(
        @Qualifier("reverseTriplegeo.configureTasklet") Tasklet tasklet)
    {
        String[] keys = new String[] { "options", "query", "input" };

        return stepBuilderFactory.get("reverseTriplegeo.configure")
            .tasklet(tasklet)
            .listener(ExecutionContextPromotionListeners.fromKeys(keys))
            .build();
    }

    @Bean("reverseTriplegeo.prepareWorkingDirectoryTasklet")
    @JobScope
    public PrepareWorkingDirectoryTasklet prepareWorkingDirectoryTasklet(
        @Value("#{jobExecutionContext['options']}") ReverseTriplegeoConfiguration options,
        @Value("#{jobExecutionContext['input']}") List<String> input,
        @Value("#{jobExecutionContext['query']}") Resource queryResource,
        @Value("#{jobExecution.jobInstance.id}") Long jobId)
    {
        Path workDir = dataDir.resolve(String.format("%05x", jobId));

        return PrepareWorkingDirectoryTasklet.builder()
            .workingDirectory(workDir)
            .input(Lists.transform(input, Paths::get))
            .inputFormat(options.getInputFormat())
            .outputFormat(options.getOutputFormat())
            .configurationGeneratorService(configurationGenerator)
            .config("options", "options.conf", options, EnumConfigurationFormat.PROPERTIES)
            .config("query", "query.sparql", queryResource)
            .build();
    }

    @Bean("reverseTriplegeo.prepareWorkingDirectoryStep")
    public Step prepareWorkingDirectoryStep(
        @Qualifier("reverseTriplegeo.prepareWorkingDirectoryTasklet") PrepareWorkingDirectoryTasklet tasklet)
        throws Exception
    {
        String[] keys = new String[] {
            "workDir", "inputDir", "inputFiles", "inputFormat", "outputDir", "outputFormat",
            "configFileByName"
        };

        return stepBuilderFactory.get("reverseTriplegeo.prepareWorkingDirectory")
            .tasklet(tasklet)
            .listener(ExecutionContextPromotionListeners.fromKeys(keys))
            .build();
    }

    @Bean("reverseTriplegeo.createContainerTasklet")
    @JobScope
    public CreateContainerTasklet createContainerTasklet(
        @Value("${slipo.rpc-server.tools.reverse-triplegeo.docker.image}") String imageName,
        @Value("#{jobExecution.jobInstance.id}") Long jobId,
        @Value("#{jobExecutionContext['workDir']}") String workDir,
        @Value("#{jobExecutionContext['inputDir']}") String inputDir,
        @Value("#{jobExecutionContext['inputFormat']}") String inputFormatName,
        @Value("#{jobExecutionContext['inputFiles']}") List<String> inputFiles,
        @Value("#{jobExecutionContext['outputDir']}") String outputDir,
        @Value("#{jobExecutionContext['configFileByName']}") Map<String, String> configFileByName)
    {
        String containerName = String.format("reverseTriplegeo-%05x", jobId);

        Path containerInputDir = containerDataDir.resolve("input");
        Path containerOutputDir = containerDataDir.resolve("output");
        Path containerConfigDir = containerDataDir;

        EnumDataFormat inputFormat = EnumDataFormat.valueOf(inputFormatName);
        String inputNameExtension = inputFormat.getFilenameExtension();

        List<String> containerInputPaths = inputFiles.stream()
            .filter(name -> StringUtils.getFilenameExtension(name).equals(inputNameExtension))
            .map(name -> containerInputDir.resolve(name).toString())
            .collect(Collectors.toList());

        return CreateContainerTasklet.builder()
            .client(docker)
            .name(containerName)
            .container(configurer -> configurer
                .image(imageName)
                .volume(Paths.get(inputDir), containerInputDir)
                .volume(Paths.get(outputDir), containerOutputDir)
                .volume(Paths.get(workDir, configFileByName.get("options")),
                    containerConfigDir.resolve("options.conf"), true)
                .volume(Paths.get(workDir, configFileByName.get("query")),
                    containerConfigDir.resolve("query.sparql"), true)
                // Set environment
                .env("INPUT_FILE", String.join(File.pathSeparator, containerInputPaths))
                .env("CONFIG_FILE", containerConfigDir.resolve("options.conf"))
                .env("SPARQL_FILE", containerConfigDir.resolve("query.sparql"))
                .env("OUTPUT_DIR", containerOutputDir)
                // Set resource limits
                .memory(memoryLimit)
                .memoryAndSwap(memorySwapLimit))
            .build();
    }

    @Bean("reverseTriplegeo.createContainerStep")
    public Step createContainerStep(
        @Qualifier("reverseTriplegeo.createContainerTasklet") CreateContainerTasklet tasklet)
        throws Exception
    {
        return stepBuilderFactory.get("reverseTriplegeo.createContainer")
            .tasklet(tasklet)
            .listener(ExecutionContextPromotionListeners.fromKeys("containerId", "containerName"))
            .build();
    }


    @Bean("reverseTriplegeo.runContainerTasklet")
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

    @Bean("reverseTriplegeo.runContainerStep")
    public Step runContainerStep(
        @Qualifier("reverseTriplegeo.runContainerTasklet") RunContainerTasklet tasklet)
        throws Exception
    {
        return stepBuilderFactory.get("reverseTriplegeo.runContainer")
            .tasklet(tasklet)
            .listener(tasklet)
            .build();
    }

    @Bean("reverseTriplegeo.createOutputArchiveTasklet")
    @JobScope
    public Tasklet createOutputArchiveTasklet(
        @Value("#{jobExecutionContext['outputFormat']}") final String outputFormatName,
        @Value("#{jobExecutionContext['outputDir']}") final String outputDir)
    {
        final EnumDataFormat outputFormat = EnumDataFormat.valueOf(outputFormatName);
        final String outputName = ReverseTriplegeoConfiguration.OUTPUT_NAME + ".zip";

        final Path archivePath = Paths.get(outputDir, outputName);

        // Collect the files to be packaged into the archive

        List<String> fileNames = null;
        switch (outputFormat) {
        case SHAPEFILE:
            fileNames = Arrays.asList("points.shp", "points.shx", "points.dbf", "points.prj");
            break;
        case CSV:
        default:
            fileNames = Arrays.asList("points.csv");
            break;
        }

        final List<Path> files = Lists.transform(fileNames, Paths.get(outputDir)::resolve);

        // Return a tasklet to carry out this step

        return new Tasklet()
        {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
                throws Exception
            {
                // Create a flat ZIP archive with everything regarded as output

                try (
                    OutputStream out = Files.newOutputStream(archivePath);
                    ZipOutputStream zout = new ZipOutputStream(out))
                {
                    for (Path file: files) {
                        String entryName = file.getFileName().toString();
                        ZipEntry entry = new ZipEntry(entryName);
                        zout.putNextEntry(entry);
                        Files.copy(file, zout);
                    }
                }

                logger.info("Created output archive: {}", archivePath);

                // Cleanup: delete original output files (a single archive will be exported)

                for (Path file: files)
                    Files.delete(file);

                return null;
            }
        };
    }

    @Bean("reverseTriplegeo.createOutputArchiveStep")
    public Step createOutputArchiveStep(
        @Qualifier("reverseTriplegeo.createOutputArchiveTasklet") Tasklet tasklet)
        throws Exception
    {
        return stepBuilderFactory.get("reverseTriplegeo.createOutputArchive")
            .tasklet(tasklet)
            .build();
    }

    /**
     * Create flow for a job expecting and reading configuration via normal {@link JobParameters}.
     */
    @Bean("reverseTriplegeo.flow")
    public Flow flow(
        @Qualifier("reverseTriplegeo.configureStep") Step configureStep,
        @Qualifier("reverseTriplegeo.prepareWorkingDirectoryStep") Step prepareWorkingDirectoryStep,
        @Qualifier("reverseTriplegeo.createContainerStep") Step createContainerStep,
        @Qualifier("reverseTriplegeo.runContainerStep") Step runContainerStep,
        @Qualifier("reverseTriplegeo.createOutputArchiveStep") Step createOutputArchiveStep)
    {
        return new FlowBuilder<Flow>("reverseTriplegeo.flow")
            .start(configureStep)
            .next(prepareWorkingDirectoryStep)
            .next(createContainerStep)
            .next(runContainerStep)
            .next(createOutputArchiveStep)
            .build();
    }

    @Bean("reverseTriplegeo.job")
    public Job job(@Qualifier("reverseTriplegeo.flow") Flow flow)
    {
        return jobBuilderFactory.get(JOB_NAME)
            .incrementer(new RunIdIncrementer())
            .listener(new LoggingJobExecutionListener())
            .start(flow)
                .end()
            .build();
    }

    @Bean("reverseTriplegeo.jobFactory")
    public JobFactory jobFactory(@Qualifier("reverseTriplegeo.job") Job job)
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
