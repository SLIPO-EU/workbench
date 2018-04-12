package eu.slipo.workbench.rpc.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
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
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumOperation;
import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.process.EnumProcessExecutionStatus;
import eu.slipo.workbench.common.model.process.EnumStepFile;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessExecutionNotFoundException;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStartException;
import eu.slipo.workbench.common.model.process.ProcessExecutionStepFileRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStepRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStopException;
import eu.slipo.workbench.common.model.process.ProcessIdentifier;
import eu.slipo.workbench.common.model.process.ProcessNotFoundException;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.common.model.process.Step;
import eu.slipo.workbench.common.model.resource.DataSource;
import eu.slipo.workbench.common.model.resource.EnumDataSourceType;
import eu.slipo.workbench.common.model.resource.FileSystemDataSource;
import eu.slipo.workbench.common.model.resource.ResourceIdentifier;
import eu.slipo.workbench.common.model.resource.ResourceMetadataCreate;
import eu.slipo.workbench.common.model.resource.ResourceRecord;
import eu.slipo.workbench.common.model.resource.UploadDataSource;
import eu.slipo.workbench.common.model.resource.UrlDataSource;
import eu.slipo.workbench.common.model.tool.MetadataRegistrationConfiguration;
import eu.slipo.workbench.common.model.tool.ToolConfiguration;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;
import eu.slipo.workbench.common.repository.AccountRepository;
import eu.slipo.workbench.common.repository.ProcessRepository;
import eu.slipo.workbench.common.repository.ProcessRepository.ProcessExecutionNotActiveException;
import eu.slipo.workbench.common.repository.ProcessRepository.ProcessHasActiveExecutionException;
import eu.slipo.workbench.common.repository.ResourceRepository;
import eu.slipo.workbench.common.service.FileNamingStrategy;
import eu.slipo.workbench.common.service.ProcessOperator;
import eu.slipo.workbench.common.service.util.PropertiesConverterService;
import eu.slipo.workflows.Workflow;
import eu.slipo.workflows.Workflow.JobDefinitionBuilder;
import eu.slipo.workflows.WorkflowBuilderFactory;
import eu.slipo.workflows.WorkflowExecutionEventListener;
import eu.slipo.workflows.WorkflowExecutionEventListenerSupport;
import eu.slipo.workflows.WorkflowExecutionSnapshot;
import eu.slipo.workflows.WorkflowExecutionStatus;
import eu.slipo.workflows.WorkflowExecutionStopListener;
import eu.slipo.workflows.exception.WorkflowExecutionStartException;
import eu.slipo.workflows.exception.WorkflowExecutionStopException;
import eu.slipo.workflows.service.WorkflowScheduler;
import eu.slipo.workflows.util.digraph.DependencyGraph;
import eu.slipo.workflows.util.digraph.DependencyGraphs;
import eu.slipo.workflows.util.digraph.TopologicalSort.CycleDetected;

@Service
public class DefaultProcessOperator implements ProcessOperator
{
    private static final Logger logger = LoggerFactory.getLogger(DefaultProcessOperator.class);

    @Autowired
    @Qualifier("userDataDirectory")
    private Path userDataDir;

    @Autowired
    @Qualifier("defaultFileNamingStrategy")
    private FileNamingStrategy userDataNamingStrategy;

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
    @Qualifier("workflowDataDirectory")
    private Path workflowDataDir;

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

    /**
     * Fix status of interrupted executions.
     *
     * <p>Before starting any execution, must clear the statuses of executions that falsely
     * appear as active (RUNNING/UNKNOWN), most commonly as a result of a non-graceful shutdown.
     */
    @PostConstruct
    private void clearRunningExecutions()
    {
        processRepository.clearRunningExecutions();
    }

    /**
     * A job-level listener for resource-related post-registration duties
     */
    private class AfterRegistrationHandler extends WorkflowExecutionEventListenerSupport
    {
        /**
         * The process execution id
         */
        private final long executionId;

        /**
         * The definition for the entire process
         */
        private final ProcessDefinition definition;

        private AfterRegistrationHandler(long executionId, ProcessDefinition definition)
        {
            this.executionId = executionId;
            this.definition = definition;
        }

        @Override
        public boolean aboutNode(String nodeName)
        {
            final Step step = definition.stepByNodeName(nodeName);
            return step != null && step.operation() == EnumOperation.REGISTER;
        }

        @Override
        public void afterNode(WorkflowExecutionSnapshot workflowExecutionSnapshot, String nodeName,
            JobExecution jobExecution)
        {
            final Step step = definition.stepByNodeName(nodeName);
            Assert.state(step.operation() == EnumOperation.REGISTER, "Expected a registration step");
            Assert.state(step.inputKeys().size() == 1, "Expected a single input for a registration step!");

            // Check batch status; proceed only if registration was successful

            final BatchStatus batchStatus = jobExecution.getStatus();
            if (batchStatus != BatchStatus.COMPLETED)
                return;

            // Determine the step that produced (as output) the input for registration step

            final int inputKey = step.inputKeys().get(0);
            final Step producerStep = definition.stepByResourceKey(inputKey);
            Assert.state(producerStep != null, "A processing step is expected to produce this resource!");

            // Extract resource (id, version) from execution context

            final String RESOURCE_ID_KEY = "resourceId";
            final String RESOURCE_VERSION_KEY = "resourceVersion";

            final ExecutionContext executionContext = jobExecution.getExecutionContext();

            long resourceId = executionContext.getLong(RESOURCE_ID_KEY, -1L);
            if (resourceId <= 0)
                throw new IllegalStateException(
                    "Expected a resource id under [" + RESOURCE_ID_KEY + "] in execution context");

            long resourceVersion = executionContext.getLong(RESOURCE_VERSION_KEY, -1L);
            if (resourceVersion <= 0)
                throw new IllegalStateException(
                    "Expected a resource version under [" + RESOURCE_VERSION_KEY + "] in execution context");

            // Associate process execution with registered resources

            resourceRepository.setProcessExecution(
                resourceId, resourceVersion, executionId, producerStep.key());
        }
    }

    /**
     * The basic workflow-level execution listener which records the status/progress of the
     * process execution
     */
    private class ReportingExecutionListener implements WorkflowExecutionEventListener
    {
        /**
         * The process execution id
         */
        private final long executionId;

        /**
         * The definition for the entire process
         */
        private final ProcessDefinition definition;

        private ReportingExecutionListener(long executionId, ProcessDefinition definition)
        {
            this.executionId = executionId;
            this.definition = definition;
        }

        @Override
        public void onSuccess(WorkflowExecutionSnapshot workflowExecutionSnapshot)
        {
            final Workflow workflow = workflowExecutionSnapshot.workflow();
            logger.info("The workflow {} has completed successfully", workflow.id());
            try {
                processRepository.updateExecution(
                    executionId, EnumProcessExecutionStatus.COMPLETED, null, ZonedDateTime.now(), null);
            } catch (ProcessExecutionNotFoundException ex) {
                throw new IllegalStateException("The execution entity has disappeared!", ex);
            }
        }

        @Override
        public void onFailure(
            WorkflowExecutionSnapshot workflowExecutionSnapshot, Map<String, List<Throwable>> failureExceptions)
        {
            final Workflow workflow = workflowExecutionSnapshot.workflow();
            logger.warn("The workflow {} has failed", workflow.id());
            try {
                processRepository.updateExecution(
                    executionId, EnumProcessExecutionStatus.FAILED, null, ZonedDateTime.now(), null);
            } catch (ProcessExecutionNotFoundException ex) {
                throw new IllegalStateException("The execution entity has disappeared!", ex);
            }
        }

        @Override
        public void beforeNode(
            WorkflowExecutionSnapshot workflowExecutionSnapshot, String nodeName, JobExecution jobExecution)
        {
            final Workflow workflow = workflowExecutionSnapshot.workflow();
            logger.info("The workflow node {}/{} has started", workflow.id(), nodeName);

            final Step step = definition.stepByNodeName(nodeName);
            if (step != null) {
                beforeProcessingStep(workflowExecutionSnapshot, step, jobExecution);
            }
        }

        private void beforeProcessingStep(
            WorkflowExecutionSnapshot workflowExecutionSnapshot, Step step, JobExecution jobExecution)
        {
            final Workflow workflow = workflowExecutionSnapshot.workflow();
            final Workflow.JobNode node = workflow.node(step.nodeName());
            final ZonedDateTime now = ZonedDateTime.now();

            // Create a record to represent this processing step

            ProcessExecutionStepRecord stepRecord = new ProcessExecutionStepRecord(step.key());
            stepRecord.setName(step.name());
            stepRecord.setNodeName(step.nodeName());
            stepRecord.setStartedOn(now);
            stepRecord.setJobExecutionId(jobExecution.getId());
            stepRecord.setStatus(EnumProcessExecutionStatus.RUNNING);
            stepRecord.setOperation(step.operation());
            stepRecord.setTool(step.tool());

            // Add the expected output file into stepRecord

            final List<Path> outputPaths = node.output();
            Assert.state(outputPaths.size() < 2, "Expected at most 1 output file");
            if (!outputPaths.isEmpty()) {
                final Path outputPath = outputPaths.get(0);
                Assert.state(outputPath.isAbsolute() && outputPath.startsWith(workflowDataDir),
                    "Expected output as an absolute path under workflow data directory");
                ProcessExecutionStepFileRecord fileRecord = new ProcessExecutionStepFileRecord(
                    EnumStepFile.OUTPUT, workflowDataDir.relativize(outputPath));
                fileRecord.setDataFormat(step.outputFormat());
                stepRecord.addFile(fileRecord);
            }

            // Todo Add configuration file(s) as file record(s) of this step
            // Check if jobExecution contains a `configFileByName` context entry. Resolve against `workDir`.

            // Update record in repository

            try {
                processRepository.createExecutionStep(executionId, stepRecord);
            } catch (ProcessExecutionNotFoundException ex) {
                throw new IllegalStateException("The execution entity has disappeared!", ex);
            } catch (ProcessExecutionNotActiveException ex) {
                throw new IllegalStateException("The execution entity is not active!", ex);
            }
        }

        @Override
        public void afterNode(
            WorkflowExecutionSnapshot workflowExecutionSnapshot, String nodeName, JobExecution jobExecution)
        {
            final Workflow workflow = workflowExecutionSnapshot.workflow();
            logger.info("The workflow node {}/{} finished with a status of [{}]",
                workflow.id(), nodeName, jobExecution.getStatus());

            final Step step = definition.stepByNodeName(nodeName);
            if (step != null) {
                afterProcessingStep(workflowExecutionSnapshot, step, jobExecution);;
            }
        }

        private void afterProcessingStep(
            WorkflowExecutionSnapshot workflowExecutionSnapshot, Step step, JobExecution jobExecution)
        {
            final Workflow workflow = workflowExecutionSnapshot.workflow();
            final Workflow.JobNode node = workflow.node(step.nodeName());
            final BatchStatus batchStatus = jobExecution.getStatus();
            final ZonedDateTime now = ZonedDateTime.now();

            ProcessExecutionRecord executionRecord = processRepository.findExecution(executionId, true);
            ProcessExecutionStepRecord stepRecord = executionRecord.getStep(step.key());
            if (stepRecord == null)
                throw new IllegalStateException(String.format(
                    "Expected to find a step record for step #%d", step.key()));

            // Update step record

            switch (batchStatus) {
            case COMPLETED:
                {
                    stepRecord.setStatus(EnumProcessExecutionStatus.COMPLETED);
                    stepRecord.setCompletedOn(now);
                    final List<Path> outputPaths = node.output();
                    // Update file record for output (with size or other computed metadata)
                    if (!outputPaths.isEmpty()) {
                        final Path outputPath = outputPaths.get(0);
                        ProcessExecutionStepFileRecord fileRecord = IterableUtils.find(
                            stepRecord.getFiles(), f -> f.getType() == EnumStepFile.OUTPUT);
                        if (fileRecord == null)
                            throw new IllegalStateException(String.format(
                                "Expected a file record for the output of step #%d of execution #%d",
                                step.key(), executionId));
                        try {
                            fileRecord.setFileSize(Files.size(outputPath));
                        } catch (IOException ex) {
                            String errMessage = String.format(
                                "The output of step#{} of execution #{} is not readable",
                                step.key(), executionId);
                            throw new IllegalStateException(errMessage, ex);
                        }
                    }
                }
                break;
            case FAILED:
                {
                    List<Throwable> failureExceptions = jobExecution.getAllFailureExceptions();
                    stepRecord.setStatus(EnumProcessExecutionStatus.FAILED);
                    stepRecord.setCompletedOn(now);
                    if (!failureExceptions.isEmpty())
                        stepRecord.setErrorMessage(failureExceptions.get(0).getMessage());
                }
                break;
            case STOPPED:
                {
                    stepRecord.setStatus(EnumProcessExecutionStatus.STOPPED);
                }
                break;
            default:
                Assert.state(false,
                    "Did not expect a batch status of ["+ batchStatus + "] in a afterNode callback");
                break;
            }

            // Update record in repository

            try {
                processRepository.updateExecutionStep(executionId, step.key(), stepRecord);
            } catch (ProcessExecutionNotFoundException ex) {
                throw new IllegalStateException("The execution entity has disappeared!", ex);
            } catch (ProcessExecutionNotActiveException ex) {
                throw new IllegalStateException("The execution entity is not active!", ex);
            }
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

    @Value("${slipo.rpc-server.workflows.salt-for-identifier:1}")
    private Long salt;

    /**
     * Compute the workflow id from the identity of a process revision
     *
     * @param id The id (parent id) of a process
     * @param version The version of a process revision
     * @return
     */
    private UUID computeWorkflowId(long id, long version)
    {
        byte[] data = ByteBuffer.wrap(new byte[3 * Long.BYTES])
            .putLong(id).putLong(version).putLong(salt)
            .array();

        return UUID.nameUUIDFromBytes(data);
    }

    /**
     * Extract an output name for data downloaded from a URL. This name will be suitable to be
     * used as the file name (under which data will be saved).
     *
     * @param url A downloadable public URL
     * @param useFragment If the URL reference (i.e. fragment) should be used as part of the output
     *   name
     */
    private String extractOutputName(URL url, boolean useFragment)
    {
        String fileName = Paths.get(url.getPath()).getFileName().toString();
        String fragment = url.getRef();
        if (!useFragment || fragment == null)
            return fileName;

        String name = StringUtils.stripFilenameExtension(fileName);
        String extension = fileName.substring(1 + name.length());
        return String.format("%s-%s.%s", name, fragment, extension);
     }

    /**
     * Build workflow from a process definition.
     *
     * @param workflowBuilder
     * @param definition The definition of a process revision
     * @param createdBy The id of the user that created the process
     * @throws MalformedURLException if encounters a malformed URL (of an external datasource)
     * @throws CycleDetected if the process definition has cyclic dependencies
     */
    private Workflow buildWorkflow(Workflow.Builder workflowBuilder, ProcessDefinition definition, int createdBy)
        throws CycleDetected
    {
        final Map<DataSource, String> sourceToNodeName = new HashMap<>();
        final Map<String, String> nodeNameToOutputName = new HashMap<>();

        // Examine referenced sources.
        // A data source of URL is handled by adding a job node that will download the
        // resource (to make it available to other workflow nodes).

        final Set<DataSource> sourcesToDownload = definition.steps().stream()
            .flatMap(step -> step.sources().stream())
            .filter(source -> source instanceof UrlDataSource)
            .collect(Collectors.toSet());

        for (DataSource source: sourcesToDownload) {
            final URL url = ((UrlDataSource) source).getUrl();
            final String nodeName = "download-" + Integer.toHexString(source.hashCode());
            final String outputName = extractOutputName(url, true);
            // Add a job node into workflow
            workflowBuilder.job(b -> b.name(nodeName)
                .flow(downloadFileFlow)
                .parameters(
                    p -> p.addString("url", url.toString()).addString("outputName", outputName))
                .output(outputName));
            // Map this source to a node name
            sourceToNodeName.put(source, nodeName);
            nodeNameToOutputName.put(nodeName, outputName);
        }

        // Map each processing step to a job node inside the workflow

        final ProcessDefinitionDependencyAnalyzer dependencyAnalyzedDefinition =
            new ProcessDefinitionDependencyAnalyzer(definition);
        for (Step step: dependencyAnalyzedDefinition.stepsInTopologicalOrder()) {
            EnumTool tool = step.tool();
            List<Integer> inputKeys = step.inputKeys();
            JobDefinitionBuilder jobDefinitionBuilder = JobDefinitionBuilder.create(step.nodeName());

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
                        Path inputPath = resolveToPath(source, createdBy);
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
                        jobDefinitionBuilder.input(dependency.nodeName(), "*");
                        inputNames.add(nodeNameToOutputName.get(dependency.nodeName()));
                    }
                }
            }

            // Define flow, parameters, and output name
            String outputName = null;
            switch (tool) {
            case REGISTER:
                {
                    Assert.state(inputKeys.size() == 1, "A registration step expects a single input");
                    Step dependency = definition.stepByResourceKey(inputKeys.get(0));
                    JobParameters parameters =
                        buildParametersForRegistration(step, dependency, definition, createdBy);
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
                throw new NotImplementedException("Α Batch flow for a tool of type [" + tool + "]");
            default:
                Assert.state(false, "Did not expect a tool of type [" + tool + "]");
            }

            // Set output name for this job node
            if (outputName != null)
                nodeNameToOutputName.put(step.nodeName(), outputName);

            // Add the job node mapping to this processing step
            workflowBuilder.job(jobDefinitionBuilder.build());
        }

        return workflowBuilder.build();
    }

    private JobParameters buildParametersForRegistration(
        Step registerStep, Step producerStep, ProcessDefinition definition, int userId)
    {
        final JobParametersBuilder parametersBuilder = new JobParametersBuilder();

        MetadataRegistrationConfiguration configuration =
            (MetadataRegistrationConfiguration) registerStep.configuration();

        EnumDataFormat format = producerStep.outputFormat();
        ToolConfiguration producerConfiguration = producerStep.configuration();

        EnumDataFormat inputFormat = EnumDataFormat.N_TRIPLES;
        if (producerStep.operation() == EnumOperation.TRANSFORM
                && (producerConfiguration instanceof TriplegeoConfiguration))
        {
            inputFormat = ((TriplegeoConfiguration) producerConfiguration).getInputFormat();
        }

        parametersBuilder.addLong("createdBy", Integer.valueOf(userId).longValue());
        parametersBuilder.addString("format", format.toString());
        parametersBuilder.addString("inputFormat", inputFormat.toString());
        parametersBuilder.addString("processName", definition.name());

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
     * Resolve a resource identifier (an instance of {@link ResourceIdentifier}) into a
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

    private Path resolveToPath(DataSource source, int createdBy)
    {
        Assert.state(source != null, "Expected a non-null source");

        Path path = null;

        final EnumDataSourceType type = source.getType();
        switch (type) {
        case UPLOAD:
            {
                path = Paths.get(((UploadDataSource) source).getPath());
                path = userDataNamingStrategy.resolvePath(createdBy, path);
            }
            break;
        case FILESYSTEM:
            {
                path = Paths.get(((FileSystemDataSource) source).getPath());
                path = userDataNamingStrategy.resolvePath(createdBy, path);
            }
            break;
        case URL:
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
     * @throws ProcessNotFoundException
     * @throws ProcessExecutionStartException
     */
    private ProcessExecutionRecord startExecution(ProcessRecord processRecord, int userId)
        throws ProcessNotFoundException, ProcessExecutionStartException
    {
        Assert.state(processRecord != null, "Expected a non-null process record");
        Assert.state(userId > 0, "Expected a valid (>0) user id");

        final long id = processRecord.getId(), version = processRecord.getVersion();
        final ProcessDefinition definition = processRecord.getDefinition();

        // Build a workflow from the definition of this process

        final int createdBy = processRecord.getCreatedBy().getId();

        final UUID workflowId = computeWorkflowId(id, version);
        final Workflow.Builder workflowBuilder = workflowBuilderFactory.get(workflowId);
        Workflow workflow = null;
        try {
            workflow = buildWorkflow(workflowBuilder, definition, createdBy);
        } catch (CycleDetected e) {
            throw new IllegalStateException("The process definition has cyclic dependencies");
        }

        // Create a new process execution entity

        ProcessExecutionRecord executionRecord = null;
        try {
            executionRecord = processRepository.createExecution(id, version, userId, workflowId);
        } catch (ProcessHasActiveExecutionException ex) {
            throw new ProcessExecutionStartException("Failed to create a new execution entity", ex);
        }
        final long executionId = executionRecord.getId();

        // Create listeners for this workflow execution

        ReportingExecutionListener reportingListener =
            new ReportingExecutionListener(executionId, definition);
        AfterRegistrationHandler registrationHandler =
            new AfterRegistrationHandler(executionId, definition);

        // Start!

        logger.info("About to start workflow {} associated with process execution #{}",
            workflow.id(), executionId);
        try {
            workflowScheduler.start(workflow, reportingListener, registrationHandler);
        } catch (WorkflowExecutionStartException ex) {
            // Discard process execution entity (the workflow execution did not even start)
            try {
                processRepository.discardExecution(executionId);
            } catch (ProcessExecutionNotFoundException ex1) {
               throw new IllegalStateException("Expected to find the execution just created!");
            }
            throw new ProcessExecutionStartException(
                String.format("Failed to start workflow (%s)", ex.getMessage()), ex);
        }

        // The execution has started: update status for process execution entity

        try {
            executionRecord = processRepository.updateExecution(
                executionId, EnumProcessExecutionStatus.RUNNING, ZonedDateTime.now(), null, null);
        } catch (ProcessExecutionNotFoundException e) {
            throw new IllegalStateException("Expected to find the execution just created!");
        }

        return executionRecord;
    }

    private void stopExecution(ProcessRecord processRecord)
        throws ProcessExecutionStopException
    {
        Assert.state(processRecord != null, "Expected a non-null process record");

        final long id = processRecord.getId(), version = processRecord.getVersion();

        // Map (id, version) of definition to the workflow identifier
        final UUID workflowId = computeWorkflowId(id, version);

        // Find latest execution (which is the only one possibly running)
        final ProcessExecutionRecord executionRecord = processRepository.findLatestExecution(id, version);
        if (executionRecord == null)
            throw new ProcessExecutionStopException("The given process has no associated executions");
        final long executionId = executionRecord.getId();

        // Stop

        WorkflowExecutionStopListener stopListener = new WorkflowExecutionStopListener()
        {
            @Override
            public void onStopped(WorkflowExecutionSnapshot workflowExecutionSnapshot)
            {
                try {
                    processRepository.updateExecution(
                        executionId, EnumProcessExecutionStatus.STOPPED, null, null, null);
                } catch (ProcessExecutionNotFoundException ex) {
                    throw new IllegalArgumentException("The execution entity has disappeared!", ex);
                }
            }
        };

        try {
            workflowScheduler.stop(workflowId, stopListener);
        } catch (WorkflowExecutionStopException ex) {
            throw new ProcessExecutionStopException("Failed to stop workflow", ex);
        }
    }

    private ProcessExecutionRecord pollStatus(ProcessRecord processRecord)
    {
        Assert.state(processRecord != null, "Expected a non-null process record");

        final long id = processRecord.getId(), version = processRecord.getVersion();
        return processRepository.findLatestExecution(id, version);
    }

    @Override
    public ProcessExecutionRecord start(long id, long version, int userId)
        throws ProcessNotFoundException, ProcessExecutionStartException
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

    @Override
    public List<ProcessIdentifier> list(boolean includeNonRunning)
    {
        List<ProcessIdentifier> processIdentifiers = new ArrayList<>();
        for (UUID workflowId: workflowScheduler.list()) {
            if (includeNonRunning || workflowScheduler.status(workflowId) == WorkflowExecutionStatus.RUNNING) {
                // Map the workflow identifier to a process identifier
                ProcessIdentifier processIdentifier =
                    processRepository.mapToProcessIdentifier(workflowId);
                Assert.state(processIdentifier != null,
                    "The workflow is not associated with a process revision entity!");
                processIdentifiers.add(processIdentifier);
            }
        }
        return processIdentifiers;
    }
}
