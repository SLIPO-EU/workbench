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
import org.springframework.batch.core.StepExecutionListener;
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

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumResourceType;
import eu.slipo.workbench.common.model.resource.EnumDataSourceType;
import eu.slipo.workbench.common.model.resource.ResourceIdentifier;
import eu.slipo.workbench.common.model.resource.ResourceMetadataCreate;
import eu.slipo.workbench.common.model.resource.ResourceRecord;
import eu.slipo.workbench.common.repository.ResourceRepository;
import eu.slipo.workbench.rpc.jobs.listener.ExecutionContextPromotionListeners;

@Component
public class RegisterResourceJobConfiguration
{
    private static Logger logger = LoggerFactory.getLogger(RegisterResourceJobConfiguration.class);

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    @Qualifier("catalogDataDirectory")
    private Path catalogDataDir;

    @Autowired
    private ResourceRepository resourceRepository;

    public class RegisterResourceTasklet implements Tasklet
    {
        private final String processName;

        private final Path inputPath;

        private final int createdBy;

        private final EnumDataFormat dataFormat;

        private final ResourceMetadataCreate metadata;

        private final ResourceIdentifier resourceIdentifier;

        public RegisterResourceTasklet(
            String processName,
            Path inputPath,
            int createdBy,
            EnumDataFormat dataFormat,
            ResourceMetadataCreate metadata,
            ResourceIdentifier resourceIdentifier)
        {
            Assert.notNull(processName,
                "The name of the creating process is required");
            Assert.notNull(metadata,
                "The user-provided metadata are required for a resource");
            Assert.notNull(metadata.getName(), "A name is required");
            Assert.isTrue(inputPath != null && inputPath.isAbsolute(),
                "An absolute input path is required");

            this.processName = processName;
            this.inputPath = inputPath;
            this.dataFormat = dataFormat;
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

            // Resolve target path under catalog root directory

            Path targetPath = Paths.get(
                Integer.valueOf(createdBy).toString(), processName, metadata.getName());
            targetPath = catalogDataDir.resolve(targetPath);

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
                // Failed to create a hard link: resort to plain copying
                Files.copy(inputPath, targetPath);
            }

            // Create a resource record

            ResourceRecord record = new ResourceRecord();
            record.setFilePath(catalogDataDir.relativize(targetPath).toString());
            record.setFileSize(Files.size(targetPath));
            record.setFormat(dataFormat);
            record.setMetadata(metadata.getName(), metadata.getDescription());
            record.setType(EnumResourceType.POI_DATA);
            record.setSourceType(EnumDataSourceType.FILESYSTEM);

            // Todo set/compute boundingBox and tableName

            // Save to repository

            if (resourceIdentifier == null) {
                record = resourceRepository.create(record, createdBy);
                logger.info("Registered as a new resource #{}: path={}",
                    record.getId(), targetPath);
            } else {
                record = resourceRepository.update(resourceIdentifier.getId(), record, createdBy);
                logger.info("Registered as a new revision #{} of resource #{}: path={}",
                    record.getVersion(), record.getId(), targetPath);
            }

            record = resourceRepository.create(record, createdBy);
            logger.info("Registered resource #{}: {}", record.getId(), record);

            // Update execution context

            executionContext.put("targetPath", targetPath.toString());
            executionContext.put("resourceId", record.getId());
            executionContext.put("resourceVersion", record.getVersion());

            return RepeatStatus.FINISHED;
        }

    }

    @Bean("registerResource.tasklet")
    @JobScope
    public RegisterResourceTasklet tasklet(
        @Value("#{jobParameters['createdBy']}") Long createdBy,
        @Value("#{jobParameters['input']}") String input,
        @Value("#{jobParameters['dataFormat']}") EnumDataFormat dataFormat,
        @Value("#{jobParameters['processName']}") String processName,
        @Value("#{jobParameters['name']}") String name,
        @Value("#{jobParameters['description']}") String description,
        @Value("#{jobParameters['resourceId']}") Long resourceId,
        @Value("#{jobExecution.jobInstance.id}") Long jobId)
    {
        Assert.isTrue(!StringUtils.isEmpty(input), "Expected an non-empty input path");
        String[] inputs = input.split(File.pathSeparator);
        Assert.isTrue(inputs.length == 1, "Expected a single input path");

        Path inputPath = Paths.get(inputs[0]);
        return new RegisterResourceTasklet(
            processName,
            inputPath,
            createdBy.intValue(),
            dataFormat,
            new ResourceMetadataCreate(name, description),
            resourceId == null? null : ResourceIdentifier.of(resourceId));
    }

    @Bean("registerResource.step")
    public Step step(@Qualifier("registerResource.tasklet") Tasklet tasklet)
        throws Exception
    {
        StepExecutionListener listener = ExecutionContextPromotionListeners
            .fromKeys("resourceId", "resourceVersion", "targetPath")
            .strict(true)
            .build();

        return stepBuilderFactory.get("registerResource")
            .tasklet(tasklet)
            .listener(listener)
            .build();
    }

    @Bean("registerResource.flow")
    public Flow flow(@Qualifier("registerResource.step") Step step)
    {
        return new FlowBuilder<Flow>("registerResource").start(step).end();
    }
}
