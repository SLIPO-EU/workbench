package eu.slipo.workbench.rpc.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
import eu.slipo.workbench.common.model.tool.AnyTool;
import eu.slipo.workbench.common.model.tool.ToolConfiguration;
import eu.slipo.workbench.common.model.tool.output.EnumOutputType;
import eu.slipo.workbench.common.model.tool.output.OutputPart;
import eu.slipo.workbench.common.repository.AccountRepository;
import eu.slipo.workbench.common.repository.ProcessRepository;
import eu.slipo.workbench.common.repository.ProcessRepository.ProcessExecutionNotActiveException;
import eu.slipo.workbench.common.repository.ProcessRepository.ProcessHasActiveExecutionException;
import eu.slipo.workbench.common.repository.ResourceRepository;
import eu.slipo.workbench.common.service.UserFileNamingStrategy;
import eu.slipo.workbench.common.service.ProcessOperator;
import eu.slipo.workbench.common.service.util.ClonerService;
import eu.slipo.workflows.Workflow;
import eu.slipo.workflows.WorkflowExecutionEventListener;
import eu.slipo.workflows.WorkflowExecutionEventListenerSupport;
import eu.slipo.workflows.WorkflowExecutionSnapshot;
import eu.slipo.workflows.WorkflowExecutionStatus;
import eu.slipo.workflows.WorkflowExecutionStopListener;
import eu.slipo.workflows.exception.WorkflowExecutionStartException;
import eu.slipo.workflows.exception.WorkflowExecutionStopException;
import eu.slipo.workflows.service.WorkflowScheduler;
import eu.slipo.workflows.util.digraph.TopologicalSort.CycleDetected;

@Service
public class DefaultProcessOperator implements ProcessOperator
{
    private static final Logger logger = LoggerFactory.getLogger(DefaultProcessOperator.class);

    @Autowired
    @Qualifier("userDataDirectory")
    private Path userDataDir;

    @Autowired
    @Qualifier("defaultUserFileNamingStrategy")
    private UserFileNamingStrategy defaultUserFileNamingStrategy;

    @Autowired
    @Qualifier("catalogDataDirectory")
    private Path catalogDataDir;

    @Autowired
    @Qualifier("catalogUserFileNamingStrategy")
    private UserFileNamingStrategy catalogUserFileNamingStrategy;

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
    private ProcessToWorkflowMapper processToWorkflowMapper;

    @Autowired
    private ClonerService cloner;

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

            // Fixme: pass outputPart to ResourceRepository.setProcessExecution ??
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
            final List<Path> inputPaths = node.input();

            final ToolConfiguration<? extends AnyTool> configuration = step.configuration();
            final EnumTool tool = step.tool();

            final ZonedDateTime now = ZonedDateTime.now();

            // Create a record for this processing step

            ProcessExecutionStepRecord stepRecord = new ProcessExecutionStepRecord(step.key());
            stepRecord.setName(step.name());
            stepRecord.setNodeName(step.nodeName());
            stepRecord.setStartedOn(now);
            stepRecord.setJobExecutionId(jobExecution.getId());
            stepRecord.setStatus(EnumProcessExecutionStatus.RUNNING);
            stepRecord.setOperation(step.operation());
            stepRecord.setTool(step.tool());

            // Add input files under step record

            final EnumDataFormat inputFormat = configuration.getInputFormat();
            for (Path inputPath: inputPaths) {
                URI inputUri = convertPathToUri(inputPath);
                Long size = null;
                try {
                    size = Files.size(inputPath);
                } catch (IOException ex) {
                    String message = String.format(
                        "Cannot stat input of execution:%d/step:%d: %s", executionId, step.key(), inputPath);
                    throw new IllegalStateException(message, ex);
                }
                ProcessExecutionStepFileRecord fileRecord =
                    new ProcessExecutionStepFileRecord(EnumStepFile.INPUT, inputUri, size, inputFormat);
                stepRecord.addFile(fileRecord);
            }

            // Add the expected output files under step record

            final OutputPart<? extends AnyTool> defaultOutputPart = tool.getDefaultOutputPart();
            final Class<?> outputPartEnumeration = tool.getOutputPartEnumeration();

            final List<Path> outputPaths = node.output();
            Assert.state(outputPaths.stream().allMatch(Path::isAbsolute),
                "An output path is expected as absolute path");
            Assert.state(outputPaths.stream().allMatch(path -> path.startsWith(workflowDataDir)),
                "An output path is expected to be under workflow data directory");

            final Map<? extends OutputPart<? extends AnyTool>, List<String>> outputMap =
                configuration.getOutputNameMapper().applyToPath(inputPaths);
            Assert.state(outputPaths.size() == outputMap.values().stream().mapToInt(List::size).sum(),
                "The number of output paths differs from the one determined by step configuration");
            Assert.state(outputMap.keySet().stream().allMatch(outputPartEnumeration::isInstance),
                "An output part is expected to be one of the enumerated parts");

            for (OutputPart<? extends AnyTool> outputPart: outputMap.keySet()) {
                Assert.state(tool.getOutputPartEnumeration().isInstance(outputPart),
                    "The outputPart is expected to be one of the enumerated parts defined by the tool");
                final EnumOutputType outputType = outputPart.outputType();
                final EnumDataFormat dataFormat = outputType == EnumOutputType.OUTPUT?
                    configuration.getOutputFormat() : null; // dataFormat only relevant to actual output results
                final EnumStepFile stepFileType = EnumStepFile.from(outputType);
                for (String outputName: outputMap.get(outputPart)) {
                    // Find corresponding item from node's output paths (must exist!)
                    Path outputPath = Iterables.find(outputPaths, path -> path.endsWith(outputName));
                    URI outputUri = convertPathToUri(outputPath);
                    ProcessExecutionStepFileRecord fileRecord =
                        new ProcessExecutionStepFileRecord(stepFileType, outputUri, null, dataFormat);
                    fileRecord.setPrimary(outputPart.equals(defaultOutputPart));
                    stepRecord.addFile(fileRecord);
                }
            }

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
            final Path stagingDir = workflow.stagingDirectory(step.nodeName());
            final BatchStatus batchStatus = jobExecution.getStatus();
            final ExecutionContext executionContext = jobExecution.getExecutionContext();

            ProcessExecutionRecord executionRecord = processRepository.findExecution(executionId, true);
            ProcessExecutionStepRecord stepRecord = executionRecord.getStep(step.key());
            if (stepRecord == null)
                throw new IllegalStateException(String.format(
                    "Expected a step record for execution:%d/step:%d", executionId, step.key()));

            //
            // Update step record
            //

            // Add configuration file(s) as file record(s) of this step.
            // Check if jobExecution context contains `configFileByName` and `workDir` entries.
            // Copy under workflow data directory (under stage/<NODE-NAME>/config-<EXECUTION-ID>)

            if (executionContext.containsKey("workDir") && executionContext.containsKey("configFileByName")) {
                Path workDir = Paths.get(executionContext.getString("workDir"));
                Assert.state(workDir != null && workDir.isAbsolute() && Files.isDirectory(workDir),
                    "Expected a directory path under `workDir` context entry");
                Map<?,?> configFileByName = (Map<?,?>) executionContext.get("configFileByName");
                Iterable<String> configNames = Iterables.filter(configFileByName.values(), String.class);
                // Create a per-execution directory to hold configuration files
                Path targetDir = stagingDir.resolve(Paths.get("config", String.format("%05x", executionId)));
                try {
                    Files.createDirectories(targetDir);
                } catch (IOException ex) {
                    throw new IllegalStateException("Cannot create directory for configuration data", ex);
                }
                // Copy under target directory, add as file records
                for (String configName: configNames) {
                    Path path = null;
                    try {
                        path = copyToTargetDirectory(workDir.resolve(configName), targetDir);
                    } catch (IOException ex) {
                        throw new IllegalStateException(ex);
                    }
                    Long size = null;
                    try {
                        size = Files.size(path);
                    } catch (IOException ex) {
                        String message = String.format(
                            "Cannot stat configuration of execution:%d/step:%d: %s",
                            executionId, step.key(), path);
                        throw new IllegalStateException(message, ex);
                    }
                    ProcessExecutionStepFileRecord fileRecord = new ProcessExecutionStepFileRecord(
                        EnumStepFile.CONFIGURATION, convertPathToUri(path), size, null);
                    stepRecord.addFile(fileRecord);
                }
            }

            // Update status, compute metadata on completed results

            final ZonedDateTime now = ZonedDateTime.now();

            switch (batchStatus) {
            case COMPLETED:
                {
                    stepRecord.setStatus(EnumProcessExecutionStatus.COMPLETED);
                    stepRecord.setCompletedOn(now);
                    // Update file records for outputs (with size or other computed metadata)
                    for (ProcessExecutionStepFileRecord fileRecord: stepRecord.getFiles()) {
                        if (!fileRecord.getType().isOfOutputType())
                            continue; // nothing to be updated
                        // A path for an output result is always relative to workflow data directory
                        Path path = Paths.get(fileRecord.getFilePath());
                        path = workflowDataDir.resolve(path);
                        try {
                            fileRecord.setFileSize(Files.size(path));
                        } catch (IOException ex) {
                            String message = String.format(
                                "Cannot stat output of execution:%d/step:%d: %s", executionId, step.key(), path);
                            throw new IllegalStateException(message, ex);
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
                throw new IllegalStateException(
                    "Did not expect a batch status of ["+ batchStatus + "] in a afterNode callback");
            }

            //
            // Update step entity in repository
            //

            try {
                processRepository.updateExecutionStep(executionId, step.key(), stepRecord);
            } catch (ProcessExecutionNotFoundException ex) {
                throw new IllegalStateException("The execution entity has disappeared!", ex);
            } catch (ProcessExecutionNotActiveException ex) {
                throw new IllegalStateException("The execution entity is not active!", ex);
            }
        }

        private Path copyToTargetDirectory(Path source, Path targetDir) throws IOException
        {
            Path target = targetDir.resolve(source.getFileName());

            Path link = null;
            try {
                link = Files.createLink(target, source);
            } catch (FileSystemException ex) {
                link = null;
            }

            // If linking has failed, fallback to copying

            if (link == null) {
                Files.copy(source, target);
            }

            return target;
        }

        /**
         * Convert a path to a URI (representing the same resource) as it should be reported to a
         * repository. This URI should (ideally) not expose server-side directory information, but
         * it should be able to be reconstruct the original path (inside the same application context).
         *
         * @param path An absolute path
         * @return a URI representing the given path
         */
        private URI convertPathToUri(Path path)
        {
            Assert.state(path != null && path.isAbsolute(), "Expected a non-null absolute path");

            URI uri = null;

            if (path.startsWith(workflowDataDir)) {
                // Convert to a relative URI with a relative path
                Path relativePath = workflowDataDir.relativize(path);
                try {
                    uri = new URI(null, null, relativePath.toString(), null);
                } catch (URISyntaxException ex) {
                    throw new IllegalArgumentException(ex);
                }
            } else if (path.startsWith(userDataDir)) {
                // Convert to an absolute user-data URI
                uri = defaultUserFileNamingStrategy.convertToUri(path);
            } else if (path.startsWith(catalogDataDir)) {
                // Convert to an absolute catalog-data URI
                uri = catalogUserFileNamingStrategy.convertToUri(path);
            } else {
                // The path doesn't reside into any of the expected locations
                throw new IllegalStateException(
                    "The path is outside of expected directory hierarchy: " + path);
            }

            return uri;
        }
    }

    private Workflow buildWorkflow(long id, long version, ProcessDefinition definition, int createdBy)
    {
        Workflow workflow = null;
        try {
            workflow = processToWorkflowMapper.buildWorkflow(id, version, definition, createdBy);
        } catch (CycleDetected e) {
            throw new IllegalStateException("The process definition has cyclic dependencies");
        }
        return workflow;
    }

    private UUID computeWorkflowId(long id, long version)
    {
        return processToWorkflowMapper.computeWorkflowId(id, version);
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

        final Workflow workflow = buildWorkflow(id, version, definition, createdBy);
        final UUID workflowId = workflow.id();

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