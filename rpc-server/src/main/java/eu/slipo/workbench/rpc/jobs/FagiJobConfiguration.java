package eu.slipo.workbench.rpc.jobs;

import static com.google.common.primitives.Ints.constrainToRange;
import static org.springframework.util.StringUtils.stripFilenameExtension;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.JobFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.StepExecutionAggregator;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.tool.FagiConfiguration;
import eu.slipo.workbench.common.model.tool.InvalidConfigurationException;
import eu.slipo.workbench.rpc.jobs.listener.ExecutionContextPromotionListeners;
import eu.slipo.workbench.rpc.jobs.listener.LoggingJobExecutionListener;
import eu.slipo.workbench.rpc.jobs.tasklet.PrepareWorkingDirectoryTasklet;
import eu.slipo.workbench.rpc.jobs.tasklet.docker.CreateContainerTasklet;
import eu.slipo.workbench.rpc.jobs.tasklet.docker.RunContainerTasklet;

@Component
public class FagiJobConfiguration extends ContainerBasedJobConfiguration
{
    private static final Logger logger = LoggerFactory.getLogger(FagiJobConfiguration.class);

    private static final String JOB_NAME = "fagi";

    public static final String FLOW_SIMPLE = "SIMPLE";

    public static final String FLOW_PARTITIONING = "PARTITIONING";

    /**
     * The default timeout (milliseconds) for a container run
     */
    public static final long DEFAULT_RUN_TIMEOUT = 60 * 1000L;

    public static final long DEFAULT_RUN_TIMEOUT_FOR_PARTITIONING = 30 * 1000L;

    public static final long DEFAULT_RUN_TIMEOUT_FOR_MERGING = 30 * 1000L;

    /**
     * The default interval (milliseconds) for polling a container
     */
    public static final long DEFAULT_CHECK_INTERVAL = 1000L;

    public static final long DEFAULT_CHECK_INTERVAL_FOR_PARTITIONING = 1000L;

    public static final long DEFAULT_CHECK_INTERVAL_FOR_MERGING = 1000L;

    /**
     * The default memory limit for a container run
     */
    public static final long DEFAULT_MEMORY_LIMIT = 536870912L;

    public static final long DEFAULT_MEMORY_LIMIT_FOR_PARTITIONING = 536870912L;

    public static final long DEFAULT_MEMORY_LIMIT_FOR_MERGING = 536870912L;

    /**
     * The maximum number of partitions (i.e. the partition size) to be created, if a job instance
     * decides to proceed with partitioning.
     * <p>
     * Note that current implementation of partitioning steps will not work for sizes > 9.
     */
    private static final int MAX_NUMBER_OF_PARTITIONS = 8;

    private String imageNameForPartitioning;

    private String imageNameForMerging;

    private long checkIntervalForPartitioning = -1L;

    private long checkIntervalForMerging = -1L;

    private long runTimeoutForPartitioning = -1L;

    private long runTimeoutForMerging = -1L;

    private long memoryLimitForPartitioning = -1L;

    private long memoryLimitForMerging = -1L;

    private long memorySwapLimitForPartitioning = -1L;

    private long memorySwapLimitForMerging = -1L;

    /**
     * A list of keys of parameters to be ignored (blacklisted) as conflicting with <tt>input</tt> parameter.
     */
    private static final List<String> blacklistedParameterKeys =
        ImmutableList.of("level", "left.path", "right.path", "links.path");

    @Autowired
    private TaskExecutor taskExecutor;

    @Override
    @Autowired
    protected void setContainerDataDirectory(
        @Value("${slipo.rpc-server.tools.fagi.docker.container-data-dir}") String dir)
    {
        super.setContainerDataDirectory(dir);
    }

    @Autowired
    private void setImage(
        @Value("${slipo.rpc-server.tools.fagi.docker.image}") String imageName)
    {
        this.imageName = imageName;
    }

    @Autowired
    private void setImageForPartitioning(
        @Value("${slipo.rpc-server.tools.fagi-partitioner.docker.image:}") String imageName)
    {
        this.imageNameForPartitioning = imageName;
    }

    @Autowired
    private void setImageForMerging(
        @Value("${slipo.rpc-server.tools.fagi-merger.docker.image:}") String imageName)
    {
        this.imageNameForMerging = imageName;
    }

    @Autowired
    private void setTimeout(
        @Value("${slipo.rpc-server.tools.fagi.timeout-seconds:}") Integer timeoutSeconds)
    {
        this.runTimeout = timeoutSeconds == null?
            DEFAULT_RUN_TIMEOUT : (timeoutSeconds.longValue() * 1000L);
    }

    @Autowired
    private void setTimeoutForPartitioning(
        @Value("${slipo.rpc-server.tools.fagi-partitioner.timeout-seconds:}") Integer timeoutSeconds)
    {
        this.runTimeoutForPartitioning = timeoutSeconds == null?
            DEFAULT_RUN_TIMEOUT_FOR_PARTITIONING : (timeoutSeconds.longValue() * 1000L);
    }

    @Autowired
    private void setTimeoutForMerging(
        @Value("${slipo.rpc-server.tools.fagi-merger.timeout-seconds:}") Integer timeoutSeconds)
    {
        this.runTimeoutForMerging = timeoutSeconds == null?
            DEFAULT_RUN_TIMEOUT_FOR_MERGING : (timeoutSeconds.longValue() * 1000L);
    }

    @Autowired
    private void setCheckInterval(
        @Value("${slipo.rpc-server.tools.fagi.check-interval-millis:}") Integer checkInterval)
    {
        this.checkInterval = checkInterval == null? DEFAULT_CHECK_INTERVAL : checkInterval.longValue();
    }

    @Autowired
    private void setCheckIntervalForPartitioning(
        @Value("${slipo.rpc-server.tools.fagi-partitioner.check-interval-millis:}") Integer checkInterval)
    {
        this.checkIntervalForPartitioning = checkInterval == null?
            DEFAULT_CHECK_INTERVAL_FOR_PARTITIONING : checkInterval.longValue();
    }

    @Autowired
    private void setCheckIntervalForMerging(
        @Value("${slipo.rpc-server.tools.fagi-merger.check-interval-millis:}") Integer checkInterval)
    {
        this.checkIntervalForMerging = checkInterval == null?
            DEFAULT_CHECK_INTERVAL_FOR_MERGING : checkInterval.longValue();
    }

    @Autowired
    private void setMemoryLimit(
        @Value("${slipo.rpc-server.tools.fagi.docker.container.memory-limit-kbytes:}") Long kbytes)
    {
        this.memoryLimit = kbytes == null? DEFAULT_MEMORY_LIMIT : (kbytes.longValue() * 1024L);
    }

    @Autowired
    private void setMemoryLimitForPartitioning(
        @Value("${slipo.rpc-server.tools.fagi-partitioner.docker.container.memory-limit-kbytes:}") Long kbytes)
    {
        this.memoryLimitForPartitioning = kbytes == null?
            DEFAULT_MEMORY_LIMIT_FOR_PARTITIONING : (kbytes.longValue() * 1024L);
    }

    @Autowired
    private void setMemoryLimitForMerging(
        @Value("${slipo.rpc-server.tools.fagi-merger.docker.container.memory-limit-kbytes:}") Long kbytes)
    {
        this.memoryLimitForMerging = kbytes == null?
            DEFAULT_MEMORY_LIMIT_FOR_MERGING : (kbytes.longValue() * 1024L);
    }

    @Autowired
    private void setMemorySwapLimit(
        @Value("${slipo.rpc-server.tools.fagi.docker.container.memoryswap-limit-kbytes:}") Long kbytes)
    {
        this.memorySwapLimit = kbytes == null? -1L : kbytes.longValue() * 1024;
    }

    @Autowired
    private void setMemorySwapLimitForPartitioning(
        @Value("${slipo.rpc-server.tools.fagi-partitioner.docker.container.memoryswap-limit-kbytes:}") Long kbytes)
    {
        this.memorySwapLimitForPartitioning = kbytes == null? -1L : kbytes.longValue() * 1024;
    }

    @Autowired
    private void setMemorySwapLimitForMerging(
        @Value("${slipo.rpc-server.tools.fagi-merger.docker.container.memoryswap-limit-kbytes:}") Long kbytes)
    {
        this.memorySwapLimitForMerging = kbytes == null? -1L : kbytes.longValue() * 1024;
    }

    @PostConstruct
    private void setMemoryLimitsIfNeeded()
    {
        if (this.memorySwapLimit < 0) {
            this.memorySwapLimit = 2L * this.memoryLimit;
        }

        if (this.memorySwapLimitForPartitioning < 0) {
            this.memorySwapLimitForPartitioning = 2L * this.memoryLimitForPartitioning;
        }

        if (this.memorySwapLimitForMerging < 0) {
            this.memorySwapLimitForMerging = 2L * this.memoryLimitForMerging;
        }

        logger.info("The memory limits are {}m/{}m",
            memoryLimit / 1024L / 1024L, memorySwapLimit / 1024L / 1024L);
    }

    @PostConstruct
    private void setupDataDirectory() throws IOException
    {
        super.setupDataDirectory("fagi");
    }

    /**
     * Represent the factors (e.g. thresholds) that affect the decision on choosing a partitioning flow
     */
    public static class PartitioningDecisionFactors
    {
        /**
         * A threshold number for the number of sameAs links
         */
        int numberOfLinksThreshold = -1;

        /**
         * A threshold number for the size (in bytes) of the largest dataset (between left and right)
         */
        long inputSizeThreshold = -1L;

        public long getInputSizeThreshold()
        {
            return inputSizeThreshold;
        }

        public int getNumberOfLinksThreshold()
        {
            return numberOfLinksThreshold;
        }

        public void setNumberOfLinksThreshold(int numberOfLinksThreshold)
        {
            this.numberOfLinksThreshold = numberOfLinksThreshold;
        }

        public void setInputSizeThresholdKbytes(long inputSizeThresholdKbytes)
        {
            this.inputSizeThreshold = inputSizeThresholdKbytes * 1024L;
        }
    }

    @ConditionalOnProperty(name = "slipo.rpc-server.tools.fagi.flow", havingValue = "partition-if-needed")
    @ConfigurationProperties(prefix = "slipo.rpc-server.tools.fagi.flow.partition-if-needed")
    @Bean("fagi.partitioningDecisionFactors")
    public PartitioningDecisionFactors  partitioningDecisionFactors()
    {
       return new PartitioningDecisionFactors();
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

            parameters = Maps.filterKeys(parameters, key -> !blacklistedParameterKeys.contains(key));

            FagiConfiguration configuration =
                propertiesConverter.propertiesToValue(parameters, FagiConfiguration.class);

            String rulesSpec = configuration.getRulesSpec();
            Assert.isTrue(rulesSpec != null && rulesSpec.matches("^(file|classpath):.*"),
                "The ruleset is expected as a file-based resource location");
            configuration.setRulesSpec("rules.xml"); // a dummy name

            String leftPath = configuration.getLeftPath();
            String rightPath = configuration.getRightPath();
            String linksPath = configuration.getLinksPath();

            List<String> inputPaths = Arrays.asList(leftPath, rightPath, linksPath);
            Assert.isTrue(!Iterables.any(inputPaths, StringUtils::isEmpty),
                "The input is expected as a triple (left, right, links) of paths");
            Assert.isTrue(Iterables.all(inputPaths, p -> Paths.get(p).isAbsolute()),
                "The input is expected as a list of absolute paths");

            configuration.clearInput();

            // Validate

            Set<ConstraintViolation<FagiConfiguration>> errors = validator.validate(configuration);
            if (!errors.isEmpty()) {
                throw InvalidConfigurationException.fromErrors(errors);
            }

            // Update execution context

            executionContext.put("spec", configuration);
            executionContext.putString("rules", rulesSpec);
            executionContext.put("input", inputPaths);

            return null;
        }
    }

    public class DecideIfPartitioningTasklet implements Tasklet
    {
        private final PartitioningDecisionFactors factors;

        private final Path leftFile;

        private final Path rightFile;

        private final Path linksFile;

        private EnumDataFormat inputFormat;

        public DecideIfPartitioningTasklet(
            PartitioningDecisionFactors factors, Path leftFile, Path rightFile, Path linksFile)
        {
            this.factors = factors;
            this.leftFile = leftFile;
            this.rightFile = rightFile;
            this.linksFile = linksFile;
        }

        public void setInputFormat(EnumDataFormat inputFormat)
        {
            this.inputFormat = inputFormat;
        }

        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
            throws Exception
        {
            StepContext stepContext = chunkContext.getStepContext();
            ExecutionContext executionContext = stepContext.getStepExecution().getExecutionContext();

            final long leftSize = Files.size(leftFile);
            final long rightSize = Files.size(rightFile);

            // The input size is the size (bytes) of largest input dataset.
            // Note: A more precise fact would be the number of triples contained in a dataset
            final long inputSize = Math.max(leftSize, rightSize);

            // Count number of links; for a file formatted as N-TRIPLE is same as the number of lines
            int numberOfLinks = -1;
            if (inputFormat == EnumDataFormat.N_TRIPLES) {
                numberOfLinks = (int) Files.lines(linksFile).count();
            }

            // Decide if partitioning is needed; if yes, also determine the number of partitions needed
            // to handle the input.
            int numberOfPartitions = -1; // a negative value means no partitioning
            if (numberOfLinks > 0) {
                if ((factors.inputSizeThreshold > 0 && inputSize > factors.inputSizeThreshold) &&
                    (factors.numberOfLinksThreshold > 0 && numberOfLinks > factors.numberOfLinksThreshold))
                {
                    numberOfPartitions = constrainToRange(
                        numberOfLinks / factors.numberOfLinksThreshold, 2, MAX_NUMBER_OF_PARTITIONS);
                }
            }

            // Update execution context with available facts
            executionContext.putLong("inputSize", inputSize);
            executionContext.putInt("numberOfLinks", numberOfLinks);
            executionContext.putInt("numberOfPartitions", numberOfPartitions);

            return null;
        }
    }

    /**
     * A tasklet that reads job parameters to a configuration bean into execution-context.
     */
    @Bean("fagi.configureTasklet")
    public Tasklet configureTasklet()
    {
        return new ConfigureTasklet();
    }

    @Bean("fagi.configureStep")
    public Step configureStep(@Qualifier("fagi.configureTasklet") Tasklet tasklet)
    {
        String[] keys = new String[] { "spec", "rules", "input" };

        return stepBuilderFactory.get("fagi.configure")
            .tasklet(tasklet)
            .listener(ExecutionContextPromotionListeners.fromKeys(keys))
            .build();
    }

    @Bean("fagi.prepareWorkingDirectoryTasklet")
    @JobScope
    public PrepareWorkingDirectoryTasklet prepareWorkingDirectoryTasklet(
        @Value("#{jobExecutionContext['spec']}") FagiConfiguration spec,
        @Value("#{jobExecutionContext['input']}") List<String> input,
        @Value("#{jobExecutionContext['rules']}") Resource rulesResource,
        @Value("#{jobExecution.jobInstance.id}") Long jobId)
    {
        Path workDir = dataDir.resolve(String.format("%05x", jobId));

        return PrepareWorkingDirectoryTasklet.builder()
            .workingDirectory(workDir)
            .input(Lists.transform(input, Paths::get))
            .inputFormat(spec.getInputFormat())
            .outputFormat(spec.getOutputFormat())
            .config("rules", "rules.xml", rulesResource)
            .build();
    }

    @Bean("fagi.prepareWorkingDirectoryStep")
    public Step prepareWorkingDirectoryStep(
        @Qualifier("fagi.prepareWorkingDirectoryTasklet") PrepareWorkingDirectoryTasklet tasklet)
        throws Exception
    {
        String[] keys = new String[] {
            "workDir", "inputDir", "inputFiles", "inputFormat", "outputDir", "outputFormat",
            "configFileByName"
        };

        return stepBuilderFactory.get("fagi.prepareWorkingDirectory")
            .tasklet(tasklet)
            .listener(tasklet)
            .listener(ExecutionContextPromotionListeners.fromKeys(keys))
            .build();
    }

    @ConditionalOnProperty(name = "slipo.rpc-server.tools.fagi.flow", havingValue = "partition-if-needed")
    @Bean("fagi.examineIfPartitioningTasklet")
    @JobScope
    public Tasklet examineIfPartitioningTasklet(
        PartitioningDecisionFactors factors,
        @Value("#{jobExecutionContext['inputFormat']}") String inputFormatName,
        @Value("#{T(java.nio.file.Paths).get(jobExecutionContext['inputDir'])}") Path inputDir,
        @Value("#{jobExecutionContext['inputFiles']}") List<String> inputFiles)
    {
        Assert.state(inputFiles != null && inputFiles.size() == 3,
            "The input is expected as a triple (left, right, links) of files");

        final Path leftFile = inputDir.resolve(inputFiles.get(0));
        final Path rightFile = inputDir.resolve(inputFiles.get(1));
        final Path linksFile = inputDir.resolve(inputFiles.get(2));

        DecideIfPartitioningTasklet tasklet =
            new DecideIfPartitioningTasklet(factors, leftFile, rightFile, linksFile);
        tasklet.setInputFormat(EnumDataFormat.valueOf(inputFormatName));

        return tasklet;
    }

    @ConditionalOnProperty(name = "slipo.rpc-server.tools.fagi.flow", havingValue = "partition-if-needed")
    @Bean("fagi.examineIfPartitioningStep")
    public Step examineIfPartitioningStep(@Qualifier("fagi.examineIfPartitioningTasklet") Tasklet tasklet)
    {
        final StepExecutionListener stepExecutionListener = ExecutionContextPromotionListeners.builder()
            .keys("inputSize", "numberOfLinks", "numberOfPartitions")
            .build();

        return stepBuilderFactory.get("fagi.examineIfPartitioning")
            .tasklet(tasklet)
            .listener(stepExecutionListener)
            .build();
    }

    @ConditionalOnProperty(name = "slipo.rpc-server.tools.fagi.flow", havingValue = "partition-if-needed")
    @Bean("fagi.flowDecider")
    @JobScope
    public JobExecutionDecider flowDecider(
        @Value("#{jobExecutionContext['numberOfLinks'] ?: -1}") Integer numberOfLinks,
        @Value("#{jobExecutionContext['numberOfPartitions'] ?: -1}") Integer numberOfPartitions)
    {
        return new JobExecutionDecider()
        {
            @Override
            public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution)
            {
                String status = numberOfLinks > 0 && numberOfPartitions > 1? FLOW_PARTITIONING : FLOW_SIMPLE;
                return new FlowExecutionStatus(status);
            }
        };
    }

    @Bean("fagi.createContainerTasklet")
    @JobScope
    public CreateContainerTasklet createContainerTasklet(
        @Value("#{jobExecution.jobInstance.id}") Long jobId,
        @Value("#{jobExecutionContext['spec']}") FagiConfiguration spec,
        @Value("#{T(java.nio.file.Paths).get(jobExecutionContext['workDir'])}") Path workDir,
        @Value("#{T(java.nio.file.Paths).get(jobExecutionContext['inputDir'])}") Path inputDir,
        @Value("#{jobExecutionContext['inputFiles']}") List<String> inputFiles,
        @Value("#{T(java.nio.file.Paths).get(jobExecutionContext['outputDir'])}") Path outputDir,
        @Value("#{jobExecutionContext['configFileByName']}") Map<String, String> configFileByName)
    {
        String containerName = String.format("fagi-%05x", jobId);

        Assert.state(inputFiles != null && inputFiles.size() == 3,
            "The input is expected as a triple (left, right, links) of files");
        String leftFileName = inputFiles.get(0);
        String rightFileName = inputFiles.get(1);
        String linksFileName = inputFiles.get(2);

        String rulesFileName = configFileByName.get("rules");

        FagiConfiguration.Input leftSpec = spec.getLeft();
        FagiConfiguration.Input rightSpec = spec.getRight();
        FagiConfiguration.Links linksSpec = spec.getLinks();

        FagiConfiguration.Output targetSpec = spec.getTarget();
        Path fusedFileName = Paths.get(targetSpec.getFusedPath()).getFileName();
        Path remainingFileName = Paths.get(targetSpec.getRemainingPath()).getFileName();
        Path reviewFileName = Paths.get(targetSpec.getReviewPath()).getFileName();
        Path statsFileName = Paths.get(targetSpec.getStatsPath()).getFileName();

        return CreateContainerTasklet.builder()
            .client(docker)
            .name(containerName)
            .container(configurer -> configurer
                .image(imageName)
                .volume(inputDir, containerDataDir.resolve("input"), true)
                .volume(outputDir, containerDataDir.resolve("output"))
                .volume(workDir.resolve(rulesFileName), containerDataDir.resolve("rules.xml"), true)
                // Set environment
                .env("VERBOSE", spec.isVerbose())
                .env("LOCALE", spec.getLang())
                .env("INPUT_FORMAT", spec.getInputFormatAsString())
                .env("OUTPUT_FORMAT", spec.getOutputFormatAsString())
                .env("SIMILARITY", spec.getSimilarityAsString())
                .env("RULES_FILE", containerDataDir.resolve("rules.xml"))
                .env("LEFT_ID", leftSpec.getId())
                .env("LEFT_FILE", containerDataDir.resolve(Paths.get("input", leftFileName)))
                .env("LEFT_DATE", Optional.ofNullable(leftSpec.getDate())
                    .map(LocalDate::toString).orElse(""))
                .env("RIGHT_ID", rightSpec.getId())
                .env("RIGHT_FILE", containerDataDir.resolve(Paths.get("input", rightFileName)))
                .env("RIGHT_DATE", Optional.ofNullable(rightSpec.getDate())
                    .map(LocalDate::toString).orElse(""))
                .env("LINKS_ID", linksSpec.getId())
                .env("LINKS_FILE", containerDataDir.resolve(Paths.get("input", linksFileName)))
                .env("LINKS_FORMAT", spec.getLinksFormatAsString())
                .env("TARGET_ID", targetSpec.getId())
                .env("TARGET_MODE", targetSpec.getModeAsString())
                .env("TARGET_FUSED_NAME", stripFilenameExtension(fusedFileName.toString()))
                .env("TARGET_REMAINING_NAME", stripFilenameExtension(remainingFileName.toString()))
                .env("TARGET_REVIEW_NAME", stripFilenameExtension(reviewFileName.toString()))
                .env("TARGET_STATS_NAME", stripFilenameExtension(statsFileName.toString()))
                .env("OUTPUT_DIR", containerDataDir.resolve("output"))
                // Set resource limits
                .memory(memoryLimit)
                .memoryAndSwap(memorySwapLimit))
            .build();
    }

    @Bean("fagi.createContainerStep")
    public Step createContainerStep(
        @Qualifier("fagi.createContainerTasklet") CreateContainerTasklet tasklet)
    {
        return stepBuilderFactory.get("fagi.createContainer")
            .tasklet(tasklet)
            .listener(ExecutionContextPromotionListeners.fromKeys("containerId", "containerName"))
            .build();
    }

    @Bean("fagi.runContainerTasklet")
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

    @Bean("fagi.runContainerStep")
    public Step runContainerStep(@Qualifier("fagi.runContainerTasklet") RunContainerTasklet tasklet)
    {
        return stepBuilderFactory.get("fagi.runContainer")
            .tasklet(tasklet)
            .listener(tasklet)
            .build();
    }

    @ConditionalOnProperty(name = "slipo.rpc-server.tools.fagi.flow", havingValue = "partition-if-needed")
    @Bean("fagi.createContainerForPartitioningTasklet")
    @JobScope
    public CreateContainerTasklet createContainerForPartitioningTasklet(
        @Value("#{jobExecution.jobInstance.id}") Long jobId,
        @Value("#{jobExecutionContext['spec']}") FagiConfiguration spec,
        @Value("#{T(java.nio.file.Paths).get(jobExecutionContext['workDir'])}") Path workDir,
        @Value("#{T(java.nio.file.Paths).get(jobExecutionContext['inputDir'])}") Path inputDir,
        @Value("#{jobExecutionContext['inputFiles']}") List<String> inputFiles,
        @Value("#{jobExecutionContext['numberOfPartitions'] ?: -1}") Integer numberOfPartitions)
        throws IOException
    {
        Assert.isTrue(numberOfPartitions >= 2, "The number of partitions should be greater or equal to 2");

        String containerName = String.format("fagi-%05x-partitioner", jobId);

        Path partitionsDir = workDir.resolve("partitions");
        try {
            Files.createDirectory(partitionsDir, DEFAULT_DIRECTORY_ATTRIBUTE);
        } catch (FileAlreadyExistsException ex) {}

        Assert.state(inputFiles != null && inputFiles.size() == 3,
            "The input is expected as a triple (left, right, links) of files");
        String leftFileName = inputFiles.get(0);
        String rightFileName = inputFiles.get(1);
        String linksFileName = inputFiles.get(2);

        FagiConfiguration.Output targetSpec = spec.getTarget();

        return CreateContainerTasklet.builder()
            .client(docker)
            .name(containerName)
            .container(configurer -> configurer
                .image(imageNameForPartitioning)
                .volume(inputDir, containerDataDir.resolve("input"), true)
                .volume(partitionsDir, containerDataDir.resolve("partitions"))
                // Set environment
                .env("INPUT_FORMAT", spec.getInputFormatAsString())
                .env("OUTPUT_FORMAT", spec.getOutputFormatAsString())
                .env("LEFT_FILE", containerDataDir.resolve(Paths.get("input", leftFileName)))
                .env("RIGHT_FILE", containerDataDir.resolve(Paths.get("input", rightFileName)))
                .env("LINKS_FILE", containerDataDir.resolve(Paths.get("input", linksFileName)))
                .env("TARGET_MODE", targetSpec.getModeAsString())
                .env("OUTPUT_DIR", containerDataDir.resolve("partitions"))
                .env("GRID_SIZE", numberOfPartitions)
                // Set resource limits
                .memory(memoryLimitForPartitioning)
                .memoryAndSwap(memorySwapLimitForPartitioning))
            .build();
    }

    @ConditionalOnProperty(name = "slipo.rpc-server.tools.fagi.flow", havingValue = "partition-if-needed")
    @Bean("fagi.createContainerForPartitioningStep")
    public Step createContainerForPartitioningStep(
        @Qualifier("fagi.createContainerForPartitioningTasklet") CreateContainerTasklet tasklet)
    {
        final StepExecutionListener stepExecutionListener = ExecutionContextPromotionListeners.builder()
            .keys("containerId", "containerName")
            .prefix("partitioner")
            .build();

        return stepBuilderFactory.get("fagi.createContainerForPartitioning")
            .tasklet(tasklet)
            .listener(stepExecutionListener)
            .build();
    }

    @ConditionalOnProperty(name = "slipo.rpc-server.tools.fagi.flow", havingValue = "partition-if-needed")
    @Bean("fagi.runContainerForPartitioningTasklet")
    @JobScope
    public RunContainerTasklet runContainerForPartitioningTasklet(
        @Value("#{jobExecutionContext['partitioner.containerName']}") String containerName)
    {
        return RunContainerTasklet.builder()
            .client(docker)
            .checkInterval(checkIntervalForPartitioning)
            .timeout(runTimeoutForPartitioning)
            .container(containerName)
            .removeOnFinished(false)
            .build();
    }

    @ConditionalOnProperty(name = "slipo.rpc-server.tools.fagi.flow", havingValue = "partition-if-needed")
    @Bean("fagi.runContainerForPartitioningStep")
    public Step runContainerForPartitioningStep(
        @Qualifier("fagi.runContainerForPartitioningTasklet") RunContainerTasklet tasklet)
    {
        return stepBuilderFactory.get("fagi.runContainerForPartitioning")
            .tasklet(tasklet)
            .listener(tasklet)
            .build();
    }

    @ConditionalOnProperty(name = "slipo.rpc-server.tools.fagi.flow", havingValue = "partition-if-needed")
    @Bean("fagi.createContainerForPartTasklet")
    @StepScope
    public CreateContainerTasklet createContainerForPartTasklet(
        @Value("#{stepExecution.jobExecution.jobInstance.id}") Long jobId,
        @Value("#{jobExecutionContext['spec']}") FagiConfiguration spec,
        @Value("#{T(java.nio.file.Paths).get(jobExecutionContext['workDir'])}") Path workDir,
        @Value("#{jobExecutionContext['configFileByName']}") Map<String, String> configFileByName,
        @Value("#{stepExecutionContext['partitionNumber']}") Integer partitionNumber)
        throws IOException
    {
        String containerName = String.format("fagi-%05x-partition-%x", jobId, partitionNumber);

        String partitionDirName = String.format("partition_%d", partitionNumber);
        Path partitionDir = workDir.resolve(Paths.get("partitions", partitionDirName));
        if (!Files.isDirectory(partitionDir)) {
            throw new IllegalStateException(
                "The input directory for partition #" + partitionNumber + " is missing: " + partitionDir);
        }

        // Create directory for the output of this partition
        Path partitionOutputDir = partitionDir.resolve("output");
        try {
            Files.createDirectory(partitionOutputDir, DEFAULT_DIRECTORY_ATTRIBUTE);
        } catch (FileAlreadyExistsException ex) {}

        EnumDataFormat inputFormat = spec.getInputFormat();
        String inputExtension = inputFormat.getFilenameExtension();

        String rulesFileName = configFileByName.get("rules");
        String leftFileName = String.format("A%d.%s", partitionNumber, inputExtension);
        String rightFileName = String.format("B%d.%s", partitionNumber, inputExtension);
        String linksFileName = String.format("links_%d.nt", partitionNumber);

        FagiConfiguration.Input leftSpec = spec.getLeft();
        FagiConfiguration.Input rightSpec = spec.getRight();
        FagiConfiguration.Links linksSpec = spec.getLinks();
        FagiConfiguration.Output targetSpec = spec.getTarget();

        return CreateContainerTasklet.builder()
            .client(docker)
            .name(containerName)
            .container(configurer -> configurer
                .image(imageName)
                .volume(partitionDir.resolve(leftFileName),
                    containerDataDir.resolve(Paths.get("input", leftFileName)), true)
                .volume(partitionDir.resolve(rightFileName),
                    containerDataDir.resolve(Paths.get("input", rightFileName)), true)
                .volume(partitionDir.resolve(linksFileName),
                    containerDataDir.resolve(Paths.get("input", linksFileName)), true)
                .volume(partitionOutputDir, containerDataDir.resolve("output"))
                .volume(workDir.resolve(rulesFileName), containerDataDir.resolve("rules.xml"), true)
                // Set environment
                .env("VERBOSE", spec.isVerbose())
                .env("LOCALE", spec.getLang())
                .env("INPUT_FORMAT", spec.getInputFormatAsString())
                .env("OUTPUT_FORMAT", spec.getOutputFormatAsString())
                .env("SIMILARITY", spec.getSimilarityAsString())
                .env("RULES_FILE", containerDataDir.resolve("rules.xml"))
                .env("LEFT_ID", leftSpec.getId())
                .env("LEFT_FILE", containerDataDir.resolve(Paths.get("input", leftFileName)))
                .env("LEFT_DATE", Optional.ofNullable(leftSpec.getDate())
                    .map(LocalDate::toString).orElse(""))
                .env("RIGHT_ID", rightSpec.getId())
                .env("RIGHT_FILE", containerDataDir.resolve(Paths.get("input", rightFileName)))
                .env("RIGHT_DATE", Optional.ofNullable(rightSpec.getDate())
                    .map(LocalDate::toString).orElse(""))
                .env("LINKS_ID", linksSpec.getId())
                .env("LINKS_FILE", containerDataDir.resolve(Paths.get("input", linksFileName)))
                .env("LINKS_FORMAT", spec.getLinksFormatAsString())
                .env("TARGET_ID", targetSpec.getId())
                .env("TARGET_MODE", targetSpec.getModeAsString())
                .env("TARGET_FUSED_NAME", FagiConfiguration.Output.DEFAULT_FUSED_NAME)
                .env("TARGET_REMAINING_NAME", FagiConfiguration.Output.DEFAULT_REMAINING_NAME)
                .env("TARGET_REVIEW_NAME", FagiConfiguration.Output.DEFAULT_REVIEW_NAME)
                .env("TARGET_STATS_NAME", FagiConfiguration.Output.DEFAULT_STATS_NAME)
                .env("OUTPUT_DIR", containerDataDir.resolve("output"))
                // Set resource limits
                .memory(memoryLimit)
                .memoryAndSwap(memorySwapLimit))
            .build();
    }

    @ConditionalOnProperty(name = "slipo.rpc-server.tools.fagi.flow", havingValue = "partition-if-needed")
    @Bean("fagi.createContainerForPartStep")
    public Step createContainerForPartStep(
        @Qualifier("fagi.createContainerForPartTasklet") Tasklet tasklet)
    {
        return stepBuilderFactory.get("fagi.createContainerForPart")
            .tasklet(tasklet)
            .build();
    }

    @ConditionalOnProperty(name = "slipo.rpc-server.tools.fagi.flow", havingValue = "partition-if-needed")
    @Bean("fagi.stepPartitioner")
    @JobScope
    public Partitioner stepPartitioner(
        @Value("#{jobExecutionContext['numberOfPartitions'] ?: -1}") Integer numberOfPartitions)
    {
        Assert.isTrue(numberOfPartitions >= 2, "The number of partitions should be greater or equal to 2");

        return new Partitioner()
        {
            @Override
            public Map<String, ExecutionContext> partition(int gridSize)
            {
                Map<String, ExecutionContext> parts = new HashMap<>();
                for (int i = 1; i <= numberOfPartitions; i++) {
                    ExecutionContext executionContext = new ExecutionContext();
                    executionContext.put("partitionNumber", i);
                    parts.put("partition" + i, executionContext);
                }
                return parts;
            }
        };
    }

    @ConditionalOnProperty(name = "slipo.rpc-server.tools.fagi.flow", havingValue = "partition-if-needed")
    @Bean("fagi.createContainerForEachPartStepAggregator")
    @JobScope
    public StepExecutionAggregator createContainerForEachPartStepAggregator(
        @Value("#{jobExecutionContext['numberOfPartitions'] ?: -1}") Integer numberOfPartitions)
    {
        Assert.isTrue(numberOfPartitions >= 2, "The number of partitions should be greater or equal to 2");

        return new StepExecutionAggregator()
        {
            @Override
            public void aggregate(StepExecution result, Collection<StepExecution> executions)
            {
                final ExecutionContext resultContext = result.getExecutionContext();

                for (StepExecution stepExecution: executions) {
                    final ExecutionContext partialContext = stepExecution.getExecutionContext();
                    final int i = partialContext.getInt("partitionNumber");
                    // Aggregate status into result: a failed part marks the result as failed
                    BatchStatus status = stepExecution.getStatus();
                    result.setStatus(BatchStatus.max(result.getStatus(), status));
                    // Merge entries for `container{Name,Id}` into result execution context
                    if (status == BatchStatus.COMPLETED) {
                        String containerName = partialContext.getString("containerName");
                        String containerId = partialContext.getString("containerId");
                        resultContext.putString("partition" + i + ".containerName", containerName);
                        resultContext.putString("partition" + i + ".containerId", containerId);
                    }
                }

                if (result.getStatus() != BatchStatus.COMPLETED) {
                    return;
                }

                // The master step is complete (all partition steps are complete)

                Map<Integer,String> containerNameByPartitionNumber = IntStream.rangeClosed(1, numberOfPartitions)
                    .boxed()
                    .collect(Collectors.toMap(Function.identity(),
                        i -> resultContext.getString("partition" + i + ".containerName")));

                Map<Integer, String> containerIdByPartitionNumber = IntStream.rangeClosed(1, numberOfPartitions)
                    .boxed()
                    .collect(Collectors.toMap(Function.identity(),
                        i -> resultContext.getString("partition" + i + ".containerId")));

                resultContext.put("containerNameByPartitionNumber", containerNameByPartitionNumber);
                resultContext.put("containerIdByPartitionNumber", containerIdByPartitionNumber);

                // Todo Aggregate container logs
                // If container logs are written under `command.output` entry, they must be also
                // be aggregated and stored under master execution context.

                return;
            }
        };
    }

    @ConditionalOnProperty(name = "slipo.rpc-server.tools.fagi.flow", havingValue = "partition-if-needed")
    @Bean("fagi.createContainerForEachPartStep")
    public Step createContainerForEachPartStep(
        @Qualifier("fagi.createContainerForPartStep") Step createContainerForPartStep,
        @Qualifier("fagi.stepPartitioner") Partitioner stepPartitioner,
        @Qualifier("fagi.createContainerForEachPartStepAggregator") StepExecutionAggregator aggregator)
    {
        // Build the master step to fork/join to N partition steps (creating N containers)

        final StepExecutionListener stepExecutionListener = ExecutionContextPromotionListeners.builder()
            .keys("containerNameByPartitionNumber", "containerIdByPartitionNumber")
            .build();

        return stepBuilderFactory.get("fagi.createContainerForEachPart")
            .partitioner("fagi.createContainerForPart", stepPartitioner)
            .aggregator(aggregator)
            .step(createContainerForPartStep)
            .taskExecutor(taskExecutor)
            .listener(stepExecutionListener)
            .build();
    }

    @ConditionalOnProperty(name = "slipo.rpc-server.tools.fagi.flow", havingValue = "partition-if-needed")
    @Bean("fagi.runContainerForPartTasklet")
    @StepScope
    public RunContainerTasklet runContainerForPartTasklet(
        @Value("#{jobExecutionContext['containerNameByPartitionNumber']}") Map<Integer,String> containerNameByPartitionNumber,
        @Value("#{stepExecutionContext['partitionNumber']}") Integer partitionNumber)
    {
        String containerName = containerNameByPartitionNumber.get(partitionNumber);
        if (StringUtils.isEmpty(containerName)) {
            throw new IllegalStateException("No entry for container for partition #" + partitionNumber);
        }

        return RunContainerTasklet.builder()
            .client(docker)
            .checkInterval(checkInterval)
            .timeout(runTimeout)
            .container(containerName)
            .removeOnFinished(false)
            .build();
    }

    @ConditionalOnProperty(name = "slipo.rpc-server.tools.fagi.flow", havingValue = "partition-if-needed")
    @Bean("fagi.runContainerForPartStep")
    public Step runContainerForPartStep(
        @Qualifier("fagi.runContainerForPartTasklet") RunContainerTasklet tasklet)
    {
        return stepBuilderFactory.get("fagi.runContainerForPart")
            .tasklet(tasklet)
            .build();
    }

    @ConditionalOnProperty(name = "slipo.rpc-server.tools.fagi.flow", havingValue = "partition-if-needed")
    @Bean("fagi.runContainerForEachPartStep")
    public Step runContainerForEachPartStep(
        @Qualifier("fagi.runContainerForPartStep") Step runContainerForPartStep,
        @Qualifier("fagi.stepPartitioner") Partitioner stepPartitioner)
    {
        // Build the master step to fork/join to N partition steps (running N containers)

        return stepBuilderFactory.get("fagi.runContainerForEachPart")
            .partitioner("fagi.runContainerForPart", stepPartitioner)
            .step(runContainerForPartStep)
            .taskExecutor(taskExecutor)
            .build();
    }

    @ConditionalOnProperty(name = "slipo.rpc-server.tools.fagi.flow", havingValue = "partition-if-needed")
    @Bean("fagi.createContainerForMergingTasklet")
    @JobScope
    public CreateContainerTasklet createContainerForMergingTasklet(
        @Value("#{jobExecution.jobInstance.id}") Long jobId,
        @Value("#{jobExecutionContext['spec']}") FagiConfiguration spec,
        @Value("#{T(java.nio.file.Paths).get(jobExecutionContext['workDir'])}") Path workDir,
        @Value("#{T(java.nio.file.Paths).get(jobExecutionContext['inputDir'])}") Path inputDir,
        @Value("#{jobExecutionContext['inputFiles']}") List<String> inputFiles,
        @Value("#{T(java.nio.file.Paths).get(jobExecutionContext['outputDir'])}") Path outputDir,
        @Value("#{jobExecutionContext['numberOfPartitions'] ?: -1}") Integer numberOfPartitions)
    {
        String containerName = String.format("fagi-%05x-merger", jobId);

        Path partitionsDir = workDir.resolve("partitions");
        Assert.state(Files.isDirectory(partitionsDir), "The partition directory should be present");

        String leftFileName = inputFiles.get(0);
        String rightFileName = inputFiles.get(1);

        FagiConfiguration.Output targetSpec = spec.getTarget();
        Path fusedFileName = Paths.get(targetSpec.getFusedPath()).getFileName();
        Path remainingFileName = Paths.get(targetSpec.getRemainingPath()).getFileName();
        Path reviewFileName = Paths.get(targetSpec.getReviewPath()).getFileName();
        Path statsFileName = Paths.get(targetSpec.getStatsPath()).getFileName();

        return CreateContainerTasklet.builder()
            .client(docker)
            .name(containerName)
            .container(configurer -> configurer
                .image(imageNameForMerging)
                .volume(inputDir.resolve(leftFileName),
                    containerDataDir.resolve(Paths.get("input", leftFileName)), true)
                .volume(inputDir.resolve(rightFileName),
                    containerDataDir.resolve(Paths.get("input", rightFileName)), true)
                .volume(partitionsDir, containerDataDir.resolve("partitions"))
                .volume(outputDir, containerDataDir.resolve("output"))
                // Set environment
                .env("INPUT_FORMAT", spec.getInputFormatAsString())
                .env("OUTPUT_FORMAT", spec.getOutputFormatAsString())
                .env("LEFT_FILE", containerDataDir.resolve(Paths.get("input", leftFileName)))
                .env("RIGHT_FILE", containerDataDir.resolve(Paths.get("input", rightFileName)))
                .env("INPUT_DIR", containerDataDir.resolve("partitions"))
                .env("TARGET_MODE", targetSpec.getModeAsString())
                .env("TARGET_FUSED_NAME", stripFilenameExtension(fusedFileName.toString()))
                .env("TARGET_REMAINING_NAME", stripFilenameExtension(remainingFileName.toString()))
                .env("TARGET_REVIEW_NAME", stripFilenameExtension(reviewFileName.toString()))
                .env("TARGET_STATS_NAME", stripFilenameExtension(statsFileName.toString()))
                .env("PARTIAL_OUTPUT_DIR_NAME", "output")
                .env("OUTPUT_DIR", containerDataDir.resolve("output"))
                .env("GRID_SIZE", numberOfPartitions)
                // Set resource limits
                .memory(memoryLimitForMerging)
                .memoryAndSwap(memorySwapLimitForMerging))
            .build();
    }

    @ConditionalOnProperty(name = "slipo.rpc-server.tools.fagi.flow", havingValue = "partition-if-needed")
    @Bean("fagi.createContainerForMergingStep")
    public Step createContainerForMergingStep(
        @Qualifier("fagi.createContainerForMergingTasklet") CreateContainerTasklet tasklet)
    {
        final StepExecutionListener stepExecutionListener = ExecutionContextPromotionListeners.builder()
            .keys("containerId", "containerName")
            .prefix("merger")
            .build();

        return stepBuilderFactory.get("fagi.createContainerForMerging")
            .tasklet(tasklet)
            .listener(stepExecutionListener)
            .build();
    }

    @ConditionalOnProperty(name = "slipo.rpc-server.tools.fagi.flow", havingValue = "partition-if-needed")
    @Bean("fagi.runContainerForMergingTasklet")
    @JobScope
    public RunContainerTasklet runContainerForMergingTasklet(
        @Value("#{jobExecutionContext['merger.containerName']}") String containerName)
    {
        return RunContainerTasklet.builder()
            .client(docker)
            .checkInterval(checkIntervalForMerging)
            .timeout(runTimeoutForMerging)
            .container(containerName)
            .removeOnFinished(false)
            .build();
    }

    @ConditionalOnProperty(name = "slipo.rpc-server.tools.fagi.flow", havingValue = "partition-if-needed")
    @Bean("fagi.runContainerForMergingStep")
    public Step runContainerForMergingStep(
        @Qualifier("fagi.runContainerForMergingTasklet") RunContainerTasklet tasklet)
    {
        return stepBuilderFactory.get("fagi.runContainerForMerging")
            .tasklet(tasklet)
            .listener(tasklet)
            .build();
    }

    /**
     * Create flow for a job expecting and reading configuration via normal {@link JobParameters}.
     */
    @ConditionalOnProperty(name = "slipo.rpc-server.tools.fagi.flow", havingValue = "simple", matchIfMissing = true)
    @Bean("fagi.flow")
    public Flow simpleFlow(
        @Qualifier("fagi.configureStep") Step configureStep,
        @Qualifier("fagi.prepareWorkingDirectoryStep") Step prepareWorkingDirectoryStep,
        @Qualifier("fagi.createContainerStep") Step createContainerStep,
        @Qualifier("fagi.runContainerStep") Step runContainerStep)
    {
        return new FlowBuilder<Flow>("fagi.flow")
            .start(configureStep)
            .next(prepareWorkingDirectoryStep)
            .next(createContainerStep)
            .next(runContainerStep)
            .build();
    }

    /**
     * Create flow for a job expecting and reading configuration via normal {@link JobParameters}.
     * <p>
     * This is a flow branching to either a simple execution or a partitioning execution. The decision
     * on which branch to follow is taken on a dedicated step, and is generally based on the size
     * (e.g actual file size or number of links) of the given input.
     */
    @ConditionalOnProperty(name = "slipo.rpc-server.tools.fagi.flow", havingValue = "partition-if-needed")
    @Bean("fagi.flow")
    public Flow partitioningFlow(
        @Qualifier("fagi.configureStep") Step configureStep,
        @Qualifier("fagi.prepareWorkingDirectoryStep") Step prepareWorkingDirectoryStep,
        @Qualifier("fagi.examineIfPartitioningStep") Step examineIfPartitioningStep,
        @Qualifier("fagi.flowDecider") JobExecutionDecider flowDecider,
        /* steps that comprise the simple flow */
        @Qualifier("fagi.createContainerStep") Step createContainerStep,
        @Qualifier("fagi.runContainerStep") Step runContainerStep,
        /* steps that comprise the partitioning flow */
        @Qualifier("fagi.createContainerForPartitioningStep") Step createContainerForPartitioningStep,
        @Qualifier("fagi.runContainerForPartitioningStep") Step runContainerForPartitioningStep,
        @Qualifier("fagi.createContainerForEachPartStep") Step createContainerForEachPartStep,
        @Qualifier("fagi.runContainerForEachPartStep") Step runContainerForEachPartStep,
        @Qualifier("fagi.createContainerForMergingStep") Step createContainerForMergingStep,
        @Qualifier("fagi.runContainerForMergingStep") Step runContainerForMergingStep)
    {
        Flow simpleFlow = new FlowBuilder<SimpleFlow>("fagi.simpleFlow")
            .start(createContainerStep)
            .next(runContainerStep)
            .build();

        Flow partitioningFlow = new FlowBuilder<SimpleFlow>("fagi.partitioningFlow")
            .start(createContainerForPartitioningStep)
            .next(runContainerForPartitioningStep)
            .next(createContainerForEachPartStep)
            .next(runContainerForEachPartStep)
            .next(createContainerForMergingStep)
            .next(runContainerForMergingStep)
            .build();

        return new FlowBuilder<Flow>("fagi.flow")
            .start(configureStep)
            .next(prepareWorkingDirectoryStep)
            .next(examineIfPartitioningStep)
            .next(flowDecider)
                .on(FLOW_PARTITIONING).to(partitioningFlow)
            .from(flowDecider)
                .on("*").to(simpleFlow)
            .build();
    }

    @Bean("fagi.job")
    public Job job(@Qualifier("fagi.flow") Flow flow)
    {
        return jobBuilderFactory.get(JOB_NAME)
            .incrementer(new RunIdIncrementer())
            .listener(new LoggingJobExecutionListener())
            .start(flow)
                .end()
            .build();
    }

    @Bean("fagi.jobFactory")
    public JobFactory jobFactory(@Qualifier("fagi.job") Job job)
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