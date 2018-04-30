package eu.slipo.workbench.rpc.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.IterableUtils;
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

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumOperation;
import eu.slipo.workbench.common.model.poi.EnumOutputType;
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
import eu.slipo.workbench.common.model.tool.ToolConfiguration;
import eu.slipo.workbench.common.repository.AccountRepository;
import eu.slipo.workbench.common.repository.ProcessRepository;
import eu.slipo.workbench.common.repository.ProcessRepository.ProcessExecutionNotActiveException;
import eu.slipo.workbench.common.repository.ProcessRepository.ProcessHasActiveExecutionException;
import eu.slipo.workbench.common.repository.ResourceRepository;
import eu.slipo.workbench.common.service.FileNamingStrategy;
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
    @Qualifier("defaultFileNamingStrategy")
    private FileNamingStrategy userDataNamingStrategy;

    @Autowired
    @Qualifier("catalogDataDirectory")
    private Path catalogDataDir;

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

            // Add the expected output files into stepRecord

            final List<Path> inputPaths = node.input();
            final List<Path> outputPaths = node.output();
            Assert.state(outputPaths.stream().allMatch(Path::isAbsolute),
                "An output path is expected as absolute path");
            Assert.state(outputPaths.stream().allMatch(path -> path.startsWith(workflowDataDir)),
                "An output path is expected to be under workflow data directory");

            final Map<EnumOutputType, List<String>> outputMap =
                determineOutputNames(step.configuration(), inputPaths);
            Assert.state(outputPaths.size() == outputMap.values().stream().mapToInt(List::size).sum(),
                "The number of output paths differs from the one determined by step configuration");

            for (EnumOutputType outputType: outputMap.keySet()) {
                EnumDataFormat outputFormat = outputType == EnumOutputType.OUTPUT?
                    step.outputFormat() : null; // output format is relevant only to actual output results
                for (String outputName: outputMap.get(outputType)) {
                    // Find corresponding item from node's output paths (must exist!)
                    Path outputPath = Iterables.find(outputPaths, path -> path.endsWith(outputName));
                    ProcessExecutionStepFileRecord fileRecord = new ProcessExecutionStepFileRecord(
                        EnumStepFile.from(outputType),
                        workflowDataDir.relativize(outputPath),
                        null,
                        outputFormat);
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
            final BatchStatus batchStatus = jobExecution.getStatus();
            final ZonedDateTime now = ZonedDateTime.now();

            ProcessExecutionRecord executionRecord = processRepository.findExecution(executionId, true);
            ProcessExecutionStepRecord stepRecord = executionRecord.getStep(step.key());
            if (stepRecord == null)
                throw new IllegalStateException(String.format(
                    "Expected to find a step record for step #%d", step.key()));

            // Todo Add configuration file(s) as file record(s) of this step
            // Check if jobExecution contains a `configFileByName` context entry. Resolve against `workDir`.
            // Move under workflow data directory (e.g. under stage/<NODE-NAME>/config)

            // Update step record

            switch (batchStatus) {
            case COMPLETED:
                {
                    stepRecord.setStatus(EnumProcessExecutionStatus.COMPLETED);
                    stepRecord.setCompletedOn(now);
                    // Update file records for outputs (with size or other computed metadata)
                    for (ProcessExecutionStepFileRecord fileRecord: stepRecord.getFiles()) {
                        EnumStepFile type = fileRecord.getType();
                        if (type == EnumStepFile.INPUT || type == EnumStepFile.CONFIGURATION)
                            continue; // nothing to update
                        Path path = Paths.get(fileRecord.getFilePath());
                        path = workflowDataDir.resolve(path);
                        try {
                            fileRecord.setFileSize(Files.size(path));
                        } catch (IOException ex) {
                            String message = String.format(
                                "The output of step#{} of execution #{} is not readable",
                                step.key(), executionId);
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

        private Map<EnumOutputType, List<String>> determineOutputNames(
            ToolConfiguration configuration, List<Path> inputPaths)
        {
            try {
                configuration = cloner.cloneAsBean(configuration);
            } catch (IOException ex) {
                throw new IllegalStateException("Cannot clone configuration", ex);
            }

            configuration = configuration
                .withInput(Lists.transform(inputPaths, Path::toString));

            return configuration.getOutputNames();
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
