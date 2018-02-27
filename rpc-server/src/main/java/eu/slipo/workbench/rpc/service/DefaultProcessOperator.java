package eu.slipo.workbench.rpc.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumOperation;
import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.process.CatalogResource;
import eu.slipo.workbench.common.model.process.EnumInputType;
import eu.slipo.workbench.common.model.process.EnumProcessExecutionStatus;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStartException;
import eu.slipo.workbench.common.model.process.ProcessExecutionStopException;
import eu.slipo.workbench.common.model.process.ProcessInput;
import eu.slipo.workbench.common.model.process.ProcessNotFoundException;
import eu.slipo.workbench.common.model.process.ProcessOutput;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.common.model.process.RegisterStep;
import eu.slipo.workbench.common.model.process.Step;
import eu.slipo.workbench.common.model.process.TransformStep;
import eu.slipo.workbench.common.model.resource.DataSource;
import eu.slipo.workbench.common.model.resource.EnumDataSourceType;
import eu.slipo.workbench.common.model.resource.ExternalUrlDataSource;
import eu.slipo.workbench.common.model.resource.FileSystemDataSource;
import eu.slipo.workbench.common.model.resource.ResourceIdentifier;
import eu.slipo.workbench.common.model.resource.ResourceMetadataCreate;
import eu.slipo.workbench.common.model.resource.ResourceRecord;
import eu.slipo.workbench.common.model.resource.UploadDataSource;
import eu.slipo.workbench.common.model.tool.MetadataRegistrationConfiguration;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;
import eu.slipo.workbench.common.repository.AccountRepository;
import eu.slipo.workbench.common.repository.ProcessRepository;
import eu.slipo.workbench.common.repository.ResourceRepository;
import eu.slipo.workbench.common.service.ProcessOperator;
import eu.slipo.workbench.common.service.util.PropertiesConverterService;
import eu.slipo.workflows.Workflow;
import eu.slipo.workflows.Workflow.JobDefinitionBuilder;
import eu.slipo.workflows.WorkflowBuilderFactory;
import eu.slipo.workflows.WorkflowExecutionEventListener;
import eu.slipo.workflows.WorkflowExecutionSnapshot;
import eu.slipo.workflows.exception.WorkflowExecutionStartException;
import eu.slipo.workflows.service.WorkflowScheduler;
import eu.slipo.workflows.util.digraph.DependencyGraph;
import eu.slipo.workflows.util.digraph.DependencyGraphs;
import eu.slipo.workflows.util.digraph.TopologicalSort.CycleDetected;

@Service
public class DefaultProcessOperator implements ProcessOperator
{
    private static final Logger logger = LoggerFactory.getLogger(DefaultProcessOperator.class);

    @Autowired
    @Qualifier("tempDataDirectory")
    private Path tempDir;

    @Autowired
    @Qualifier("catalogDataDirectory")
    private Path catalogDataDir;

    @Autowired
    private PropertiesConverterService propertiesConverter;

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private WorkflowScheduler workflowScheduler;

    @Autowired
    private WorkflowBuilderFactory workflowBuilderFactory;

    @Autowired
    @Qualifier("triplegeo.flow")
    private Flow triplegeoFlow;

    @Autowired
    @Qualifier("registerResource.flow")
    private Flow registerResourceFlow;

    @Autowired
    @Qualifier("downloadFile.flow")
    private Flow downloadFileFlow;

    private class ExecutionListener implements WorkflowExecutionEventListener
    {
        private final long executionId;

        private final long userId;

        private final ProcessDefinition definition;

        public ExecutionListener(long executionId, long userId, ProcessDefinition definition)
        {
            this.executionId = executionId;
            this.userId = userId;
            this.definition = definition;
        }

        @Override
        public void onSuccess(WorkflowExecutionSnapshot workflowExecutionSnapshot)
        {
            final Workflow workflow = workflowExecutionSnapshot.workflow();

            // Todo DefaultProcessOperator$ExecutionListener.onSuccess
            logger.info("The workflow {} has completed successfully", workflow.id());
        }

        @Override
        public void onFailure(WorkflowExecutionSnapshot workflowExecutionSnapshot)
        {
            final Workflow workflow = workflowExecutionSnapshot.workflow();

            // Todo DefaultProcessOperator$ExecutionListener.onFailure

            logger.warn("The workflow {} has failed", workflow.id());
        }

        @Override
        public void beforeNode(
            WorkflowExecutionSnapshot snapshot, String nodeName, JobExecution jobExecution)
        {
            final Workflow workflow = snapshot.workflow();

            // Todo DefaultProcessOperator$ExecutionListener.beforeNode

            logger.info("The workflow {} has started node [{}]",
                workflow.id(), nodeName);
        }

        @Override
        public void afterNode(
            WorkflowExecutionSnapshot snapshot, String nodeName, JobExecution jobExecution)
        {
            final Workflow workflow = snapshot.workflow();

            final Step step = definition.stepByName(nodeName);

            // Todo DefaultProcessOperator$ExecutionListener.afterNode

            ExecutionContext executionContext = jobExecution.getExecutionContext();
            logger.info(
                "The workflow {} has finished node [{}] with status of [{}]; context={}",
                workflow.id(), nodeName, jobExecution.getStatus(), executionContext);


        }
    }

    private static class ProcessDefinitionDependencyAnalyzer
    {
        private final ProcessDefinition definition;

        public ProcessDefinitionDependencyAnalyzer(ProcessDefinition definition)
        {
            this.definition = definition;
        }

        /**
         * Iterate on steps in topological order.
         * @throws CycleDetected
         */
        public Iterable<Step> stepsInTopologicalOrder()
            throws CycleDetected
        {
            final List<Step> steps = definition.steps();

            // Build dependency graph

            final DependencyGraph dependencyGraph = DependencyGraphs.create(steps.size());
            for (Step step: steps) {
                for (Integer inputKey: step.inputKeys()) {
                    Step dependency = definition.stepByResourceKey(inputKey);
                    if (dependency != null)
                        dependencyGraph.addDependency(step.key(), dependency.key());
                }
            }

            final Iterable<Integer> keysInTopologicalOrder =
                DependencyGraphs.topologicalSort(dependencyGraph);

            return IterableUtils.transformedIterable(keysInTopologicalOrder, k -> definition.stepByKey(k));
        }
    }

    /**
     * Build workflow from a process definition.
     *
     * @param workflowBuilder
     * @param definition The definition of a process revision
     * @param userId
     * @throws MalformedURLException if encounters a malformed URL (of an external datasource)
     * @throws CycleDetected if the process definition has cyclic dependencies
     */
    private void buildWorkflow(Workflow.Builder workflowBuilder, ProcessDefinition definition, int userId)
        throws CycleDetected
    {
        final Map<DataSource, String> sourceToNodeName = new HashMap<>();
        final Map<String, String> nodeNameToOutputName = new HashMap<>();

        // Examine referenced sources.
        // A data source of EXTERNAL_URL is handled by adding a job node that will download
        // the resource (to make it available to other workflow nodes).

        final Set<DataSource> sourcesThatMustDownload = definition.steps().stream()
            .flatMap(step -> step.sources().stream())
            .filter(source -> source instanceof ExternalUrlDataSource)
            .collect(Collectors.toSet());

        for (DataSource source: sourcesThatMustDownload) {
            final URL url = ((ExternalUrlDataSource) source).getUrl();
            final String fileName = Paths.get(url.getPath()).getFileName().toString();
            final String nodeName = "download-" + Integer.toHexString(source.hashCode());
            // Add a job node into workflow
            workflowBuilder.job(b -> b.name(nodeName)
                .flow(downloadFileFlow)
                .parameters(
                    p -> p.addString("url", url.toString()).addString("outputName", fileName))
                .output(fileName));
            // Map this source to a node name
            sourceToNodeName.put(source, nodeName);
            nodeNameToOutputName.put(nodeName, fileName);
        }

        // Map each processing step to a job node inside the workflow

        final ProcessDefinitionDependencyAnalyzer dependencyAnalyzedDefinition =
            new ProcessDefinitionDependencyAnalyzer(definition);
        for (Step step: dependencyAnalyzedDefinition.stepsInTopologicalOrder()) {
            EnumTool tool = step.tool();
            List<Integer> inputKeys = step.inputKeys();
            JobDefinitionBuilder jobDefinitionBuilder = JobDefinitionBuilder.create(step.name());

            // Define inputs for this job node
            List<String> inputNames = new ArrayList<>();
            if (!step.sources().isEmpty()) {
                // This step imports data from a data source (external to the application)
                Assert.state(inputKeys.isEmpty(), "Expected no input keys for this step");
                for (DataSource source: step.sources()) {
                    String dependencyName = sourceToNodeName.get(source);
                    if (dependencyName != null) {
                        jobDefinitionBuilder.input(dependencyName, "*");
                        inputNames.add(nodeNameToOutputName.get(dependencyName));
                    } else {
                        Path inputPath = resolveToPath(source);
                        jobDefinitionBuilder.input(inputPath);
                        inputNames.add(inputPath.getFileName().toString());
                    }
                }
            } else {
                // This step expects input from a catalog resource or from another step
                Assert.state(!inputKeys.isEmpty(), "Did not expect an empty list of input keys");
                for (Integer inputKey: inputKeys) {
                    ResourceIdentifier resourceIdentifier =
                        definition.resourceIdentifierByResourceKey(inputKey);
                    if (resourceIdentifier != null) {
                        // The input is a registered file resource
                        Path inputPath = resolveToPath(resourceIdentifier);
                        jobDefinitionBuilder.input(inputPath);
                        inputNames.add(inputPath.getFileName().toString());
                    } else {
                        // The input is the output of another step we depend on
                        Step dependency = definition.stepByResourceKey(inputKey);
                        Assert.state(dependency != null,
                            "Expected a step to produce the input we depend on!");
                        jobDefinitionBuilder.input(dependency.name(), "*");
                        inputNames.add(nodeNameToOutputName.get(dependency.name()));
                    }
                }
            }

            // Define flow, parameters, and output name
            String outputName = null;
            switch (tool) {
            case REGISTER_METADATA:
                {
                    Assert.state(inputKeys.size() == 1, "A registration step expects a single input");
                    Step dependency = definition.stepByResourceKey(inputKeys.get(0));
                    JobParameters parameters =
                        buildParametersForRegistration(step, dependency, definition.name(), userId);
                    jobDefinitionBuilder
                        .flow(registerResourceFlow)
                        .parameters(parameters);
                }
                break;
            case TRIPLEGEO:
                {
                    Assert.state(inputNames.size() == 1, "A transformation step expects a single input");
                    TriplegeoConfiguration configuration = (TriplegeoConfiguration) step.configuration();
                    EnumDataFormat outputFormat = configuration.getOutputFormat();
                    outputName = StringUtils.stripFilenameExtension(inputNames.get(0)) + "."
                        + outputFormat.getFilenameExtension();
                    jobDefinitionBuilder
                        .flow(triplegeoFlow)
                        .parameters(propertiesConverter.valueToProperties(configuration))
                        .output(outputName);
                }
                break;
            case LIMES:
            case DEER:
            case FAGI:
                throw new NotImplementedException("Spring-Batch flow for a tool of type [" + tool + "]");
            default:
                Assert.state(false, "Did not expect a tool of type [" + tool + "]");
            }

            // Set output name for this job node
            if (outputName != null)
                nodeNameToOutputName.put(step.name(), outputName);

            // Add the job node mapping to this processing step
            workflowBuilder.job(jobDefinitionBuilder.build());
        }

    }

    private JobParameters buildParametersForRegistration(
        Step registerStep, Step producerStep, String procName, int userId)
    {
        final JobParametersBuilder parametersBuilder = new JobParametersBuilder();

        final MetadataRegistrationConfiguration configuration =
            (MetadataRegistrationConfiguration) registerStep.configuration();

        final EnumDataFormat dataFormat = producerStep.outputFormat();

        parametersBuilder.addLong("createdBy", Integer.valueOf(userId).longValue());
        parametersBuilder.addString("dataFormat", dataFormat.toString());
        parametersBuilder.addString("processName", procName);

        ResourceMetadataCreate metadata = configuration.getMetadata();
        parametersBuilder.addString("name", metadata.getName());
        if (metadata.getDescription() != null) {
            parametersBuilder.addString("description", metadata.getDescription());
        }

        ResourceIdentifier target = configuration.getTarget();
        if (target != null) {
            parametersBuilder.addLong("resourceId", target.getId());
        }

        return parametersBuilder.toJobParameters();
    }

    /**
     * Resolve a reference to a resource (an instance of {@link CatalogResource}) to a
     * resource record.
     *
     * @param resourceIdentifier A pair of id and version
     * @return a full-fledged record as returned from resource repository
     *
     * @throws NoSuchElementException if the pair of (id,version) does not correspond to
     *   a registered resource.
     */
    private ResourceRecord findResourceRecord(ResourceIdentifier resourceIdentifier)
    {
        Assert.state(resourceIdentifier != null, "Expected a non-null resource identifier");

        final long id = resourceIdentifier.getId(), version = resourceIdentifier.getVersion();
        Assert.state(id > 0, "The resource id is not valid");
        Assert.state(version > 0, "The resource version is not valid");

        ResourceRecord record = resourceRepository.findOne(id, version);
        if (record == null)
            throw new NoSuchElementException(
                String.format("The resource does not exist (id=%d, version=%d)", id, version));

        return record;
    }

    private Path resolveToPath(ResourceIdentifier resourceIdentifier)
    {
        ResourceRecord record = findResourceRecord(resourceIdentifier);
        return resolveToPath(record);
    }

    private Path resolveToPath(ResourceRecord record)
    {
        Assert.state(record != null, "Expected a non-null record");

        final String p = record.getFilePath();
        Assert.state(!StringUtils.isEmpty(p), "Expected a non-empty relative path");

        return catalogDataDir.resolve(p);
    }

    private Path resolveToPath(DataSource source)
    {
        Assert.state(source != null, "Expected a non-null source");

        Path path = null;

        final EnumDataSourceType type = source.getType();
        switch (type) {
        case UPLOAD:
            path = tempDir.resolve(((UploadDataSource) source).getPath());
            break;
        case FILESYSTEM:
            path = tempDir.resolve(((FileSystemDataSource) source).getPath());
            break;
        case EXTERNAL_URL:
            throw new UnsupportedOperationException(
                "A data source of [" + type + "] cannot be resolved to a local file path");
        case HARVESTER:
            throw new NotImplementedException(
                "A data source of type [" + type + "] is not supported (yet)");
        default:
            Assert.state(false, "Did not expect a source type of type [" + type + "]");
        }

        return path;
    }

    /**
     * Start the execution of a workflow. The workflow is derived from definition of
     * a process.
     *
     * <p>Note: A workflow is 1-1 mapped to a process record (which in turn is a view of
     * a process revision entity). As a consequence, a process revision can only have a single
     * active execution at a given time (because a workflow does so).
     *
     * @param processRecord The process record (representing a specific revision)
     * @param userId
     * @return
     * @throws ProcessNotFoundException
     * @throws ProcessExecutionStartException
     * @throws MalformedURLException
     * @throws WorkflowExecutionStartException
     */
    private ProcessExecutionRecord startExecution(ProcessRecord processRecord, int userId)
        throws ProcessNotFoundException, ProcessExecutionStartException
    {
        Assert.state(processRecord != null, "Expected a non-null process record");
        Assert.state(userId > 0, "Expected a valid (>0) user id");

        final long id = processRecord.getId(), version = processRecord.getVersion();
        final ProcessDefinition definition = processRecord.getDefinition();

        // Map (id, version) of definition to the workflow identifier

        final UUID workflowId = UUID.nameUUIDFromBytes(
            ByteBuffer.wrap(new byte[16])
                .putLong(id).putLong(version)
                .array());

        // Build a workflow from the definition of this process

        final Workflow.Builder workflowBuilder = workflowBuilderFactory.get(workflowId);
        try {
            buildWorkflow(workflowBuilder, definition, userId);
        } catch (CycleDetected e) {
            throw new IllegalStateException("The process definition has cyclic dependencies");
        }

        // Create a process-execution entity and associate with captured events

        ProcessExecutionRecord executionRecord =
            processRepository.createExecution(id, version, userId);
        final long executionId = executionRecord.getId();

        // Add workflow listeners targeting specific nodes (or group of nodes)

        // Fixme afterRegisterHandler
//        final WorkflowExecutionEventListener afterRegisterHandler = null; // Todo
//        definition.steps().stream()
//            .filter(step -> step.operation() == EnumOperation.REGISTER)
//            .forEach(step -> workflowBuilder.listener(step.name(), afterRegisterHandler));

        final Workflow workflow = workflowBuilder.build();
        logger.info("About to start workflow #{}", workflow.id());

        ExecutionListener listener = new ExecutionListener(executionId, userId, definition);

        try {
            workflowScheduler.start(workflow, listener);
        } catch (WorkflowExecutionStartException ex) {
            throw new ProcessExecutionStartException("failed to start workflow", ex);
        }

        // Update status for process execution entity

        executionRecord.setStartedOn(ZonedDateTime.now());
        executionRecord.setStatus(EnumProcessExecutionStatus.RUNNING);
        executionRecord = processRepository.updateExecution(executionId, executionRecord);
        return executionRecord;
    }

    private void stopExecution(ProcessRecord process)
    {
        // Todo Find and stop workflow associated with process of given (id,version)

        throw new NotImplementedException("Todo");
    }

    private ProcessExecutionRecord pollStatus(ProcessRecord process)
    {
        // Todo Poll the status of workflow associated with process of given (id,version)

        throw new NotImplementedException("Todo");
    }

    @Override
    public ProcessExecutionRecord start(long id, long version, int userId)
        throws ProcessNotFoundException, ProcessExecutionStartException, IOException
    {
        Assert.isTrue(userId < 0 || accountRepository.exists(userId), "No user with given id");

        ProcessRecord processRecord = processRepository.findOne(id, version);
        if (processRecord == null)
            throw new ProcessNotFoundException(id, version);

        if (userId < 0)
            userId = processRecord.getCreatedBy().getId();

        return startExecution(processRecord, userId);
    }

    @Override
    public void stop(long id, long version)
        throws ProcessNotFoundException, ProcessExecutionStopException
    {
        ProcessRecord r = processRepository.findOne(id, version);
        if (r == null)
            throw new ProcessNotFoundException(id, version);
        stopExecution(r);
    }

    @Override
    public ProcessExecutionRecord poll(long id, long version)
    {
        ProcessRecord r = processRepository.findOne(id, version);
        return r == null? null : pollStatus(r);
    }

}
