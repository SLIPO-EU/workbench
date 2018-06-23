package eu.slipo.workbench.rpc.jobs;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
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

import com.github.slugify.Slugify;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumResourceType;
import eu.slipo.workbench.common.model.resource.EnumDataSourceType;
import eu.slipo.workbench.common.model.resource.ResourceIdentifier;
import eu.slipo.workbench.common.model.resource.ResourceMetadataCreate;
import eu.slipo.workbench.common.model.resource.ResourceRecord;
import eu.slipo.workbench.common.repository.ResourceRepository;
import eu.slipo.workbench.common.service.UserFileNamingStrategy;
import eu.slipo.workbench.rpc.jobs.listener.ExecutionContextPromotionListeners;

@Component
public class RegisterToCatalogJobConfiguration
{
    /**
     * The key of an entry (under execution context) holding the id of a registered resource
     */
    public static final String RESOURCE_ID_KEY = "resourceId";

    /**
     * The key of an entry (under execution context) holding the version of a registered resource
     */
    public static final String RESOURCE_VERSION_KEY = "resourceVersion";

    /**
     * The key of an entry (under execution context) holding the absolute file path to the resource
     */
    public static final String PATH_KEY = "path";

    private static final String INPUT_PATH_KEY = "inputPath";

    private static final Logger logger = LoggerFactory.getLogger(RegisterToCatalogJobConfiguration.class);

    private static final Slugify slugify = new Slugify();

    private static String slugify(String text)
    {
        return slugify.slugify(text);
    }

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    @Qualifier("catalogDataDirectory")
    private Path catalogDataDir;

    @Autowired
    @Qualifier("catalogUserFileNamingStrategy")
    private UserFileNamingStrategy catalogUserFileNamingStrategy;

    @Autowired
    private ResourceRepository resourceRepository;

    public class RegisterResourceTasklet implements Tasklet
    {
        private final String processName;

        private final Path inputPath;

        private final int createdBy;

        private final EnumDataFormat format;

        private final EnumDataFormat inputFormat;

        private final ResourceMetadataCreate metadata;

        private final ResourceIdentifier resourceIdentifier;

        private RegisterResourceTasklet(
            String processName,
            Path inputPath,
            int createdBy,
            EnumDataFormat format,
            EnumDataFormat inputFormat,
            ResourceMetadataCreate metadata,
            ResourceIdentifier resourceIdentifier)
        {
            this.processName = processName;
            this.inputPath = inputPath;
            this.format = format;
            this.inputFormat = inputFormat;
            this.createdBy = createdBy;
            this.metadata = metadata;
            this.resourceIdentifier = resourceIdentifier;
        }

        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
            throws Exception
        {
            StepContext stepContext = chunkContext.getStepContext();
            ExecutionContext executionContext = stepContext.getStepExecution().getExecutionContext();

            // Check if already complete (a restart of a stopped, yet complete, execution)

            if (executionContext.containsKey(RESOURCE_ID_KEY)) {
                // Assume the task is complete
                Assert.state(executionContext.getLong(RESOURCE_ID_KEY, -1L) > 0,
                    "The tasklet is expected to write a resource id into execution context");
                Assert.state(executionContext.getLong(RESOURCE_VERSION_KEY, -1L) > 0,
                    "The tasklet is expected to write a resource version into execution context");
                return RepeatStatus.FINISHED;
            }

            // Compose name and resolve target path under catalog root directory

            String targetName = slugify(metadata.getName()) + "." + format.getFilenameExtension();
            Path targetPath = catalogUserFileNamingStrategy.resolvePath(createdBy, targetName);
            Assert.state(targetPath.startsWith(catalogDataDir),
                "The target path is expected to be under catalog data directory");

            // Create parent directories if needed

            try {
                Files.createDirectories(targetPath.getParent());
            } catch (FileAlreadyExistsException ex) {
                // no-op
            }

            // Copy

            try {
                Files.createLink(targetPath, inputPath);
            } catch (FileSystemException ex) {
                Files.copy(inputPath, targetPath); // fallback to plain copying
            }

            // Create a resource record

            ResourceRecord record = new ResourceRecord();
            record.setFilePath(catalogDataDir.relativize(targetPath).toString());
            record.setFileSize(Files.size(targetPath));
            record.setFormat(format);
            record.setInputFormat(inputFormat);
            record.setMetadata(metadata.getName(), metadata.getDescription());
            record.setType(EnumResourceType.POI_DATA);
            record.setSourceType(EnumDataSourceType.FILESYSTEM);

            // Save to repository

            if (resourceIdentifier == null) {
                record = resourceRepository.create(record, createdBy);
                logger.info("Registered as a new resource #{}: {}", record.getId(), targetPath);
            } else {
                record = resourceRepository.update(resourceIdentifier.getId(), record, createdBy);
                logger.info("Registered as a new revision #{} of resource #{}: {}",
                    record.getVersion(), record.getId(), targetPath);
            }

            // Update execution context

            executionContext.put(INPUT_PATH_KEY, inputPath.toString());
            executionContext.put(PATH_KEY, targetPath.toString());
            executionContext.put(RESOURCE_ID_KEY, record.getId());
            executionContext.put(RESOURCE_VERSION_KEY, record.getVersion());

            return RepeatStatus.FINISHED;
        }
    }

    @Bean("registerToCatalog.tasklet")
    @JobScope
    public RegisterResourceTasklet tasklet(
        @Value("#{jobParameters['input']}") String input,
        @Value("#{jobParameters['createdBy']}") Long createdBy,
        @Value("#{jobParameters['format']}") EnumDataFormat format,
        @Value("#{jobParameters['inputFormat']}") EnumDataFormat inputFormat,
        @Value("#{jobParameters['processName']}") String processName,
        @Value("#{jobParameters['name']}") String name,
        @Value("#{jobParameters['description']}") String description,
        @Value("#{jobParameters['resourceId']}") Long resourceId,
        @Value("#{jobExecution.jobInstance.id}") Long jobId)
    {
        Assert.notNull(processName, "Expected a name for the creating process");
        Assert.notNull(createdBy, "Expected a user id for the creator of this resource");
        Assert.isTrue(format != null && format != EnumDataFormat.UNDEFINED,
            "Expected a data format for this resource");

        Assert.notNull(name, "Expected a user-provided name for a resource");
        ResourceMetadataCreate metadata = new ResourceMetadataCreate(name, description);

        Assert.isTrue(!StringUtils.isEmpty(input), "Expected an non-empty input path");
        String[] inputs = input.split(File.pathSeparator);
        Assert.isTrue(inputs.length == 1, "A registration step expects a single input path");

        Path inputPath = Paths.get(inputs[0]);
        Assert.notNull(inputPath, "A path is required!");
        Assert.isTrue(inputPath.isAbsolute(), "An absolute file path is required for a resource");

        return new RegisterResourceTasklet(
            processName,
            inputPath,
            createdBy.intValue(),
            format,
            inputFormat,
            metadata,
            resourceId == null? null : ResourceIdentifier.of(resourceId));
    }

    @Bean("registerToCatalog.step")
    public Step step(@Qualifier("registerToCatalog.tasklet") Tasklet registerTasklet)
        throws Exception
    {
        String[] keys = new String[] { RESOURCE_ID_KEY, RESOURCE_VERSION_KEY, PATH_KEY };

        return stepBuilderFactory.get("registerToCatalog")
            .tasklet(registerTasklet)
            .listener(ExecutionContextPromotionListeners.fromKeys(keys))
            .build();
    }

    @Bean("registerToCatalog.flow")
    public Flow flow(@Qualifier("registerToCatalog.step") Step step)
    {
        return new FlowBuilder<Flow>("registerToCatalog").start(step).end();
    }
}
