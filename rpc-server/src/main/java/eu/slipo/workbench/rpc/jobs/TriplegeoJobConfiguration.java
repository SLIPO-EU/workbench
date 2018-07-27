package eu.slipo.workbench.rpc.jobs;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.tool.EnumConfigurationFormat;
import eu.slipo.workbench.common.model.tool.InvalidConfigurationException;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;
import eu.slipo.workbench.rpc.jobs.listener.ExecutionContextPromotionListeners;
import eu.slipo.workbench.rpc.jobs.listener.LoggingJobExecutionListener;
import eu.slipo.workbench.rpc.jobs.tasklet.PrepareWorkingDirectoryTasklet;
import eu.slipo.workbench.rpc.jobs.tasklet.docker.CreateContainerTasklet;
import eu.slipo.workbench.rpc.jobs.tasklet.docker.RunContainerTasklet;
import jersey.repackaged.com.google.common.collect.Iterables;

@Component
public class TriplegeoJobConfiguration extends ContainerBasedJobConfiguration
{
    private static final Logger logger = LoggerFactory.getLogger(TriplegeoJobConfiguration.class);

    private static final String JOB_NAME = "triplegeo";

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
        @Value("${slipo.rpc-server.tools.triplegeo.docker.container-data-dir}") String dir)
    {
        super.setContainerDataDirectory(dir);
    }

    @Autowired
    private void setTimeout(
        @Value("${slipo.rpc-server.tools.triplegeo.timeout-seconds:}") Integer timeoutSeconds)
    {
        this.runTimeout = timeoutSeconds == null? DEFAULT_RUN_TIMEOUT : (timeoutSeconds.longValue() * 1000L);
    }

    @Autowired
    private void setCheckInterval(
        @Value("${slipo.rpc-server.tools.triplegeo.check-interval-millis:}") Integer checkInterval)
    {
        this.checkInterval = checkInterval == null? DEFAULT_CHECK_INTERVAL : checkInterval.longValue();
    }

    @Autowired
    private void setMemoryLimit(
        @Value("${slipo.rpc-server.tools.triplegeo.container.memory.memory-limit-kbytes:}") Long kbytes)
    {
        this.memoryLimit = kbytes == null? DEFAULT_MEMORY_LIMIT : (kbytes.longValue() * 1024L);
    }

    @Autowired
    private void setMemorySwapLimit(
        @Value("${slipo.rpc-server.tools.triplegeo.container.memory.memoryswap-limit-kbytes:}") Long kbytes)
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
        super.setupDataDirectory("triplegeo");
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

            TriplegeoConfiguration configuration =
                propertiesConverter.propertiesToValue(parameters, TriplegeoConfiguration.class);

            String mappingSpec = configuration.getMappingSpec();
            Assert.isTrue(mappingSpec != null && mappingSpec.matches("^(file|classpath):.*"),
                "The mappings are expected as a file-based resource location");
            configuration.setMappingSpec("mappings.yml"); // a dummy name

            String classificationSpec = configuration.getClassificationSpec();
            Assert.isTrue(classificationSpec != null && classificationSpec.matches("^(file|classpath):.*"),
                "The classification is expected as a file-based resource location");
            configuration.setClassificationSpec("classification.csv"); // a dummy name

            List<String> inputPaths = new ArrayList<>(configuration.getInput());
            Assert.isTrue(!inputPaths.isEmpty() && !Iterables.any(inputPaths, StringUtils::isEmpty),
                "The input is expected as a non-empty list of paths");
            Assert.isTrue(Iterables.all(inputPaths, p -> Paths.get(p).isAbsolute()),
                "The input is expected as a list of absolute paths");
            configuration.clearInput();

            // Validate options

            Set<ConstraintViolation<TriplegeoConfiguration>> errors = validator.validate(configuration);
            if (!errors.isEmpty()) {
                throw InvalidConfigurationException.fromErrors(errors);
            }

            // Check if output format is supported
            // The only supported output format is N-TRIPLES: this is a limitation applied by
            // workbench (i.e. not by Triplegeo) in order to easily concatenate output results.
            Assert.state(configuration.getOutputFormat() == EnumDataFormat.N_TRIPLES,
                "The given output format is not supported (inside SLIPO workbench)");

            // Update execution context

            executionContext.put("options", configuration);
            executionContext.putString("mappings", mappingSpec);
            executionContext.putString("classification", classificationSpec);
            executionContext.put("input", inputPaths);

            return null;
        }
    }

    /**
     * A tasklet that reads job parameters to a configuration bean into execution-context.
     */
    @Bean("triplegeo.configureTasklet")
    public Tasklet configureTasklet()
    {
        return new ConfigureTasklet();
    }

    @Bean("triplegeo.configureStep")
    public Step configureStep(
        @Qualifier("triplegeo.configureTasklet") Tasklet tasklet)
    {
        String[] keys = new String[] { "options", "mappings", "classification", "input" };

        return stepBuilderFactory.get("triplegeo.configure")
            .tasklet(tasklet)
            .listener(ExecutionContextPromotionListeners.fromKeys(keys))
            .build();
    }

    @Bean("triplegeo.prepareWorkingDirectoryTasklet")
    @JobScope
    public PrepareWorkingDirectoryTasklet prepareWorkingDirectoryTasklet(
        @Value("#{jobExecutionContext['options']}") TriplegeoConfiguration options,
        @Value("#{jobExecutionContext['input']}") List<String> input,
        @Value("#{jobExecutionContext['mappings']}") Resource mappingsResource,
        @Value("#{jobExecutionContext['classification']}") Resource classificationResource,
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
            .config("mappings", "mappings.yml", mappingsResource)
            .config("classification", "classification.csv", classificationResource)
            .build();
    }

    @Bean("triplegeo.prepareWorkingDirectoryStep")
    public Step prepareWorkingDirectoryStep(
        @Qualifier("triplegeo.prepareWorkingDirectoryTasklet") PrepareWorkingDirectoryTasklet tasklet)
        throws Exception
    {
        String[] keys = new String[] {
            "workDir", "inputDir", "inputFiles", "inputFormat", "outputDir", "outputFormat",
            "configFileByName"
        };

        return stepBuilderFactory.get("triplegeo.prepareWorkingDirectory")
            .tasklet(tasklet)
            .listener(ExecutionContextPromotionListeners.fromKeys(keys))
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
        @Value("#{jobExecutionContext['configFileByName']}") Map<String, String> configFileByName)
    {
        String containerName = String.format("triplegeo-%05x", jobId);

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
                .volume(Paths.get(workDir, configFileByName.get("mappings")),
                    containerConfigDir.resolve("mappings.yml"), true)
                .volume(Paths.get(workDir, configFileByName.get("classification")),
                    containerConfigDir.resolve("classification.csv"), true)
                // Set environment
                .env("INPUT_FILE", String.join(File.pathSeparator, containerInputPaths))
                .env("CONFIG_FILE", containerConfigDir.resolve("options.conf"))
                .env("MAPPINGS_FILE", containerConfigDir.resolve("mappings.yml"))
                .env("CLASSIFICATION_FILE", containerConfigDir.resolve("classification.csv"))
                .env("OUTPUT_DIR", containerOutputDir)
                // Set resource limits
                .memory(memoryLimit)
                .memoryAndSwap(memorySwapLimit))
            .build();
    }

    @Bean("triplegeo.createContainerStep")
    public Step createContainerStep(
        @Qualifier("triplegeo.createContainerTasklet") CreateContainerTasklet tasklet)
        throws Exception
    {
        return stepBuilderFactory.get("triplegeo.createContainer")
            .tasklet(tasklet)
            .listener(ExecutionContextPromotionListeners.fromKeys("containerId", "containerName"))
            .build();
    }

    @Bean("triplegeo.runContainerTasklet")
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

    @Bean("triplegeo.runContainerStep")
    public Step runContainerStep(@Qualifier("triplegeo.runContainerTasklet") RunContainerTasklet tasklet)
        throws Exception
    {
        return stepBuilderFactory.get("triplegeo.runContainer")
            .tasklet(tasklet)
            .listener(tasklet)
            .build();
    }

    /**
     * A tasklet to concatenate transformation result with classification output
     */
    @Bean("triplegeo.concatenateOutputTasklet")
    @JobScope
    public Tasklet concatenateOutputTasklet(
        @Value("#{jobExecutionContext['inputFormat']}") String inputFormatName,
        @Value("#{jobExecutionContext['inputFiles']}") List<String> inputFiles,
        @Value("#{jobExecutionContext['outputFormat']}") String outputFormatName,
        @Value("#{jobExecutionContext['outputDir']}") String outputDir)
    {
        final EnumDataFormat inputFormat = EnumDataFormat.valueOf(inputFormatName);
        final String inputNameExtension = inputFormat.getFilenameExtension();

        final EnumDataFormat outputFormat = EnumDataFormat.valueOf(outputFormatName);
        Assert.state(outputFormat == EnumDataFormat.N_TRIPLES,
            "The given output format does not support concatenation of results!");

        final List<String> inputNames = inputFiles.stream()
            .filter(name -> StringUtils.getFilenameExtension(name).equals(inputNameExtension))
            .map(StringUtils::stripFilenameExtension)
            .collect(Collectors.toList());

        final Path classificationFile = Paths.get(outputDir, "classification.nt");
        final Path tmpDir = Paths.get(outputDir);

        return new Tasklet()
        {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
                throws Exception
            {
                // Concatenate each result with classification output

                for (String inputName: inputNames) {
                    Path outputFile = Paths.get(outputDir, inputName + ".nt");
                    Path resultFile = Files.createTempFile(tmpDir, null, null);
                    // Concatenate
                    try (OutputStream out = Files.newOutputStream(resultFile)) {
                        out.write("# Classification\n".getBytes());
                        Files.copy(classificationFile, out);
                        out.write("# Transformed\n".getBytes());
                        Files.copy(outputFile, out);
                    }
                    // Replace original result
                    Files.move(outputFile, Paths.get(outputDir, inputName + ".nt.orig"));
                    Files.move(resultFile, outputFile);
                }

                return null;
            }
        };
    }

    @Bean("triplegeo.concatenateOutputStep")
    public Step concatenateOutputStep(@Qualifier("triplegeo.concatenateOutputTasklet") Tasklet tasklet)
        throws Exception
    {
        return stepBuilderFactory.get("triplegeo.concatenateOutput")
            .tasklet(tasklet).build();
    }

    /**
     * A tasklet to link actual results to expected output names (i.e to make them consistent with
     * provided input name).
     */
    @Bean("triplegeo.linkToOutputTasklet")
    @JobScope
    public Tasklet linkToOutputTasklet(
        @Value("#{jobExecutionContext['input']}") List<String> input,
        @Value("#{jobExecutionContext['inputFiles']}") List<String> inputFiles,
        @Value("#{jobExecutionContext['outputFormat']}") String outputFormatName,
        @Value("#{jobExecutionContext['outputDir']}") String outputDir)
    {
        final EnumDataFormat outputFormat = EnumDataFormat.valueOf(outputFormatName);
        final String outputNameExtension = outputFormat.getFilenameExtension();

        return new Tasklet()
        {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
                throws Exception
            {
                // If a single ZIP archive is provided as input, the input name may differ from
                // the name of a contained entry (supplied as an actual input to Triplegeo).

                if (input.size() > 1) {
                    // If an archive is provided as input, it is always expected as a single input
                    return null; // no-op
                }

                // Examine our input

                Path inputPath = Paths.get(input.get(0));
                if ("application/zip".equals(Files.probeContentType(inputPath))) {
                    String expectedName =
                        StringUtils.stripFilenameExtension(inputPath.getFileName().toString());
                    String actualName =
                        StringUtils.stripFilenameExtension(inputFiles.get(0));
                    if (!expectedName.equals(actualName)) {
                        // Link result under expected name
                        Files.createSymbolicLink(
                            Paths.get(outputDir, expectedName + "." + outputNameExtension),
                            Paths.get(actualName + "." + outputNameExtension));
                        // Link execution KPI metadata under expected name
                        Files.createSymbolicLink(
                            Paths.get(outputDir, expectedName + "_metadata.json"),
                            Paths.get(actualName + "_metadata.json"));
                        // Link registration output (if it exists) under expected name
                        if (Files.exists(Paths.get(outputDir, actualName + ".csv"))) {
                            Files.createSymbolicLink(
                                Paths.get(outputDir, expectedName + ".csv"),
                                Paths.get(actualName + ".csv"));
                        }
                    }
                }

                return null;
            }
        };
    }

    @Bean("triplegeo.linkToOutputStep")
    public Step linkToOutputStep(@Qualifier("triplegeo.linkToOutputTasklet") Tasklet tasklet)
        throws Exception
    {
        return stepBuilderFactory.get("triplegeo.linkToOutput")
            .tasklet(tasklet).build();
    }

    /**
     * Create flow for a job expecting and reading configuration via normal {@link JobParameters}.
     */
    @Bean("triplegeo.flow")
    public Flow flow(
        @Qualifier("triplegeo.configureStep") Step configureStep,
        @Qualifier("triplegeo.prepareWorkingDirectoryStep") Step prepareWorkingDirectoryStep,
        @Qualifier("triplegeo.createContainerStep") Step createContainerStep,
        @Qualifier("triplegeo.runContainerStep") Step runContainerStep,
        @Qualifier("triplegeo.concatenateOutputStep") Step concatenateOutputStep,
        @Qualifier("triplegeo.linkToOutputStep") Step linkToOutputStep)
    {
        return new FlowBuilder<Flow>("triplegeo.flow")
            .start(configureStep)
            .next(prepareWorkingDirectoryStep)
            .next(createContainerStep)
            .next(runContainerStep)
            .next(concatenateOutputStep)
            .next(linkToOutputStep)
            .build();
    }

    @Bean("triplegeo.job")
    public Job job(@Qualifier("triplegeo.flow") Flow flow)
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
