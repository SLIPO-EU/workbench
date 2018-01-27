package eu.slipo.workflows.service;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.util.Pair;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.stereotype.Service;

import eu.slipo.workflows.Workflow;
import eu.slipo.workflows.WorkflowExecution;
import eu.slipo.workflows.WorkflowExecutionCompletionListener;
import eu.slipo.workflows.WorkflowExecutionEventListener;
import eu.slipo.workflows.WorkflowExecutionListener;
import eu.slipo.workflows.WorkflowExecutionSnapshot;
import eu.slipo.workflows.WorkflowExecutionStopListener;
import eu.slipo.workflows.exception.WorkflowExecutionAlreadyCompleteException;
import eu.slipo.workflows.exception.WorkflowExecutionAlreadyRunningException;
import eu.slipo.workflows.exception.WorkflowExecutionAlreadyStoppingException;
import eu.slipo.workflows.exception.WorkflowExecutionNotRunningException;
import eu.slipo.workflows.exception.WorkflowExecutionNotStartedException;
import eu.slipo.workflows.exception.WorkflowExecutionStartException;
import eu.slipo.workflows.exception.WorkflowExecutionStopException;
import eu.slipo.workflows.exception.WorkflowExecutionStuckException;
import eu.slipo.workflows.util.concurrent.LazyUtils;

@Service
public class EventBasedWorkflowScheduler extends AbstractWorkflowScheduler
    implements InitializingBean  
{
    private static final boolean debug = true;
    
    private static Logger logger = LoggerFactory.getLogger(EventBasedWorkflowScheduler.class);
    
    private static DateFormatter DATE_FORMATTER = new DateFormatter("yyyy-MM-dd'T'HH:mm:ssZ");
    
    private static final Set<BatchStatus> AFTER_STATUSES = 
        EnumSet.of(BatchStatus.COMPLETED, BatchStatus.FAILED, BatchStatus.STOPPED); 
    
    private static final Set<BatchStatus> BEFORE_STATUSES = 
        EnumSet.of(BatchStatus.STARTED, BatchStatus.STOPPING);

    private static final boolean FIX_TRANSIENT_STATUSES = true;
    
    private static String formatDate(Date t)
    {
        return DATE_FORMATTER.print(t, Locale.ROOT);
    }
    
    /**
     * A snapshot of the control-block of a scheduled workflow execution
     */
    private static class ControlSnapshot implements ExecutionSnapshot
    {
        private final WorkflowExecutionSnapshot workflowExecutionSnapshot;
        
        private final ExecutionStatus status;
        
        private final Iterable<WorkflowExecutionListener> listeners;
        
        public ControlSnapshot(
            ExecutionStatus status, 
            WorkflowExecutionSnapshot workflowExecutionSnapshot,
            Iterator<WorkflowExecutionListener> listenerIterator)
        {
            Validate.notNull(workflowExecutionSnapshot, "The workflow execution snapshot is required");
            this.workflowExecutionSnapshot = workflowExecutionSnapshot;
            this.status = status;
            this.listeners = listenerIterator == null? 
                Collections.emptyList() : LazyUtils.lazyIterable(listenerIterator);
        }
        
        public ControlSnapshot(
            ExecutionStatus status, WorkflowExecutionSnapshot workflowExecutionSnapshot)
        {
            this(status, workflowExecutionSnapshot, null);
        }
        
        @Override
        public ExecutionStatus status()
        {   
            return status;
        }
        
        @Override
        public WorkflowExecutionSnapshot workflowExecutionSnapshot()
        {
            return workflowExecutionSnapshot;
        }
        
        public Iterable<WorkflowExecutionListener> getListeners()
        {
            return listeners;
        }
    }
    
    /**
     * The control-block for a scheduled workflow execution
     */
    private class ControlBlock 
    {
        private final Workflow workflow;
        
        private final WorkflowExecution workflowExecution;
        
        /** 
         * The mapping from node names to Batch jobs 
         */
        private final Map<String,Job> jobByNodeName;
        
        /** 
         * The aggregate status for the workflow execution 
         */
        private volatile ExecutionStatus status = null;
        
        /** 
         * A flag that indicates that the workflow execution has encountered at least 
         * one failure. 
         */
        private volatile boolean failed = false;
       
        /** 
         * A flag that indicates that the workflow execution is requested to stop. 
         */
        private volatile boolean stopRequested = false;

        /** 
         * A list of execution event listeners 
         */
        private List<WorkflowExecutionListener> listeners;
       
        /** 
         * The timestamp (as milliseconds since Epoch) of the last update received from
         * an afterNode/beforeNode callback. 
         */
        private volatile long lastUpdated = 0L;
        
        /**
         * A flag to mark this control block as expired. A block can expire only if not
         * running (i.e completed, failed or stopped), and also a certain amount of time
         * has elapsed since its last update.
         */
        private volatile boolean expired = false;
        
        private ControlBlock(Workflow workflow)
        {            
            this.workflow = workflow;
            this.workflowExecution = new WorkflowExecution(workflow);
            
            // Map node names to Batch jobs (do not burden synchronized parts)
            
            this.jobByNodeName = Collections.unmodifiableMap(
                IterableUtils.toList(workflow.nodes()).stream()
                    .map(y -> Pair.of(y.name(), buildJob(y)))
                    .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
        }
        
        public ExecutionStatus status()
        {
            return status;
        }
        
        public ExecutionStatus getStatus()
        {
            return status;
        }
        
        public Long getLastUpdated()
        {
            return lastUpdated;
        }
        
        public synchronized ControlSnapshot snapshot()
        {
            return new ControlSnapshot(status, workflowExecution.snapshot());
        }
        
        public boolean checkExpired(long now)
        {
            if (expired)
                return true;
            else if (status == null || status == ExecutionStatus.RUNNING)
                return false;
            
            synchronized (this) {
                if (!expired &&
                        (status == ExecutionStatus.COMPLETED || status == ExecutionStatus.FAILED) &&
                        (now - lastUpdated > maxDurationAfterUpdate))
                    expired = true;
                return expired;
            }
        }
        
        public boolean isExpired()
        {
            return expired;
        }
        
        public synchronized void startExecution(WorkflowExecutionCompletionListener... callbacks) 
            throws WorkflowExecutionStartException
        {                        
            if (status == ExecutionStatus.COMPLETED) {
                // A completed workflow may not be restarted
                throw new WorkflowExecutionAlreadyCompleteException();
            } else if (status == ExecutionStatus.RUNNING) {
                // The execution is running (only one instance of this execution may be running at a time) 
                throw new WorkflowExecutionAlreadyRunningException();
            } else if (expired) {
                // The block is expired (failed with no activity for a certain amount of time), and about
                // to be garbage-collected by a cleanup thread.
                Validate.validState(status == ExecutionStatus.FAILED);
                throw new WorkflowExecutionStartException("The execution is expired and marked for removal");
            }
            
            // The execution can be started (either a newly created, failed, or stopped)
            
            Validate.validState(status == null || 
                    status == ExecutionStatus.FAILED || status == ExecutionStatus.STOPPED,
                "Did not expect an execution status of %s", status);
            
            // Load (or reload) workflow execution from job repository
            
            workflowExecution.load(jobRepository, FIX_TRANSIENT_STATUSES);
            Validate.validState(!workflowExecution.isRunning(), 
                "Did not expect a non-running workflow execution to contain running nodes!");

            // Check if complete, else check if restartable
            
            if (workflowExecution.isComplete()) {
                Validate.validState(status == null || status == ExecutionStatus.STOPPED);
                status = ExecutionStatus.COMPLETED;
                throw new WorkflowExecutionAlreadyCompleteException();
            } else if (workflowExecution.isAbandoned()) {
                status = ExecutionStatus.FAILED;
                throw new WorkflowExecutionStuckException(
                    "The execution contains nodes marked as abandoned (which are not restartable)");
            }
            
            List<Workflow.JobNode> readyNodes = IterableUtils.toList(
                IterableUtils.transformedIterable(workflowExecution.readyNodes(), x -> x.node()));
            if (readyNodes.isEmpty())
                throw new WorkflowExecutionStuckException(
                    "The execution is not complete, yet nothing can be restarted");

            // Reset flags and listeners
            
            failed = false;
            stopRequested = false;
            listeners = new CopyOnWriteArrayList<>(callbacks);

            // Move to running execution status, start ready nodes
            
            ExecutionStatus previousStatus = status;
            status = ExecutionStatus.RUNNING;
            if (debug) debugRunning(previousStatus);
            startExecution(readyNodes);
        }
        
        public synchronized void stopExecution(WorkflowExecutionStopListener... callbacks)
            throws WorkflowExecutionStopException
        {
            if (status == ExecutionStatus.RUNNING) {
                // Check if stopping and set flag
                if (stopRequested)
                    throw new WorkflowExecutionAlreadyStoppingException();
                stopRequested = true;
                // Add listeners for STOPPED event
                listeners.addAll(Arrays.asList(callbacks));
                // Send stop request to job executions of running nodes
                for (WorkflowExecution.NodeExecution x: workflowExecution.nodes()) {
                    if (x.status().isRunning()) {
                        logger.debug("Stopping job execution#{}", x.executionId());
                        try {
                            jobOperator.stop(x.executionId());
                        } catch (NoSuchJobExecutionException ex) {
                            throw new IllegalStateException(ex);
                        } catch (JobExecutionNotRunningException ex) {
                            // no-op
                        }
                    }
                }
            } else {
                throw new WorkflowExecutionNotRunningException();
            }
        }
        
        private Job buildJob(Workflow.JobNode node)
        {
            final Flow flow = node.flow(stepBuilderFactory);
            
            return jobBuilderFactory.get(node.jobName())
                .start(flow)
                    .end()
                .listener(new ExecutionEventListener(workflow.id(), node.name()))
                .build();
        }
        
        private void startExecution(Iterable<Workflow.JobNode> readyToStartNodes)
        {
            for (Workflow.JobNode node: readyToStartNodes) {
                Job job = jobByNodeName.get(node.name());
                JobParameters jobParameters = node.parameters();
                JobExecution jobExecution = null;
                try {
                    jobExecution = jobLauncher.run(job, jobParameters);
                    logger.info("Started node {} as job execution #{}", node, jobExecution.getId());
                } catch (JobExecutionException e) {
                    jobExecution = null;
                    logger.warn("The node {} failed to start: {}", node, e.getMessage());
                }
                if (jobExecution != null) {
                    // The workflow execution must update node to a running status
                    workflowExecution.update(node.name(), jobExecution);
                }
            }
        }
        
        public void afterNode(String nodeName, JobExecution jobExecution) 
        {            
            final BatchStatus batchStatus = jobExecution.getStatus();
            final Workflow.JobNode node =  workflow.node(nodeName);
                
            Validate.validState(AFTER_STATUSES.contains(batchStatus), 
                "A batch status of %s not expected for an afterJob callback", batchStatus);
            
            lastUpdated = jobExecution.getLastUpdated().getTime();
            
            ControlSnapshot snapshot = updateAfterNode(node, jobExecution);
            WorkflowExecutionSnapshot workflowExecutionSnapshot = snapshot.workflowExecutionSnapshot();
            
            Iterable<WorkflowExecutionListener> listeners = 
                IterableUtils.chainedIterable(workflow.getListeners(), snapshot.getListeners());
            
            // Fire registered afterNode listeners
           
            invokeAfterNodeListeners(listeners, nodeName, jobExecution, workflowExecutionSnapshot);
            
            // Check if finished, fire listeners
            
            // Note that if the status is reported as non RUNNING, we can safely invoke "once"
            // listeners here (without having to also synchronize this part). That is because at 
            // most one update can report a transition RUNNING -> {COMPLETED,STOPPED,FAILED}.
            
            if (debug && snapshot.status() != ExecutionStatus.RUNNING)
                debugFinished();
            
            switch (snapshot.status()) {
            case FAILED:
                {
                    // Fire failure listeners
                    invokeFailureListeners(listeners, workflowExecutionSnapshot);
                }
                break;
            case COMPLETED:
                {
                    // Complete execution (e.g. copy output to target directory)
                    completeExecution(workflowExecutionSnapshot);
                    // Fire success listeners
                    invokeSuccessListeners(listeners, workflowExecutionSnapshot);
                }
                break;
            case STOPPED:
                {
                    // Fire stop listeners
                    invokeStopListeners(listeners, workflowExecutionSnapshot);
                }
                break;
            case RUNNING:
            default:
                // no-op
            }
        }
    
        private synchronized ControlSnapshot updateAfterNode(Workflow.JobNode node, JobExecution jobExecution)
        {
            Validate.validState(status == ExecutionStatus.RUNNING, 
                "Expected a running execution, not %s", status);
            
            final BatchStatus batchStatus = jobExecution.getStatus();
            
            List<Workflow.JobNode> readyNodes = workflowExecution.update(node.name(), jobExecution);
            WorkflowExecutionSnapshot workflowExecutionSnapshot = workflowExecution.snapshot();
            
            // Follow-up on workflow execution, or check if finished
            
            switch (batchStatus) {
            case COMPLETED:
                {
                    if (workflowExecutionSnapshot.isComplete()) {
                        // The entire workflow is complete
                        // Update overall execution status (no other afterNode callback expected)
                        status = ExecutionStatus.COMPLETED;
                    } else if (failed) {
                        // If nothing reported as running during our update, then we can
                        // update overall execution status (no other afterNode callback expected)
                        if (!workflowExecutionSnapshot.isRunning()) {
                            status = ExecutionStatus.FAILED;
                            if (debug) debugFailed(workflowExecutionSnapshot);
                        }
                    } else if (stopRequested) {
                        // see above (same explanation as if failed)
                        if (!workflowExecutionSnapshot.isRunning()) {
                            status = failed? ExecutionStatus.FAILED : ExecutionStatus.STOPPED;
                            if (debug) debugStopped(workflowExecutionSnapshot);
                        }
                    } else {
                        // Some dependent nodes became ready during our update
                        if (!readyNodes.isEmpty()) {
                            logger.debug("The node {} is complete;" +
                                    " Starting dependent nodes that became ready: {}", 
                                node, readyNodes);
                            startExecution(readyNodes);
                        } else {
                            logger.debug("The node {} is complete", node);
                        }
                    }
                }
                break;
            case FAILED:
                {
                    failed = true;
                    // see case of COMPLETED (same explanation as if failed) 
                    if (!workflowExecutionSnapshot.isRunning()) {
                        status = ExecutionStatus.FAILED;
                        if (debug) debugFailed(workflowExecutionSnapshot);
                    }
                }
                break;
            case STOPPED:
                {
                    // see case of COMPLETED (same explanation as if stopRequested) 
                    if (stopRequested && !workflowExecutionSnapshot.isRunning()) {
                        status = failed? ExecutionStatus.FAILED : ExecutionStatus.STOPPED;
                        if (debug) debugStopped(workflowExecutionSnapshot);
                    }
                }
                break;
            default:
                Validate.validState(false,
                    "A batch status of %s not expected for an afterJob callback", batchStatus);
            }
            
            return new ControlSnapshot(status, workflowExecutionSnapshot, listeners.iterator());
        }

        public void beforeNode(String nodeName, JobExecution jobExecution)
        {
            final BatchStatus batchStatus = jobExecution.getStatus();
            final Workflow.JobNode node =  workflow.node(nodeName);
            
            Validate.validState(BEFORE_STATUSES.contains(batchStatus),
                "A batch status of %s not expected for an beforeJob callback", batchStatus);
            
            lastUpdated = jobExecution.getLastUpdated().getTime();
            
            ControlSnapshot snapshot = updateBeforeNode(node, jobExecution);
            WorkflowExecutionSnapshot workflowExecutionSnapshot = snapshot.workflowExecutionSnapshot();
            
            Iterable<WorkflowExecutionListener> listeners = 
                IterableUtils.chainedIterable(workflow.getListeners(), snapshot.getListeners());
            
            // Fire registered beforeNode listeners
            
            invokeBeforeNodeListeners(listeners, nodeName, jobExecution, workflowExecutionSnapshot);
        }
        
        private synchronized ControlSnapshot updateBeforeNode(Workflow.JobNode node, JobExecution jobExecution)
        {            
            Validate.validState(status == ExecutionStatus.RUNNING, 
                "Expected a running execution, not %s", status);
            
            List<Workflow.JobNode> readyNodes = workflowExecution.update(node.name(), jobExecution);
            WorkflowExecutionSnapshot workflowExecutionSnapshot = workflowExecution.snapshot();
            
            Validate.validState(readyNodes.isEmpty(), 
                "Did not expect any nodes to become ready during this update!");
            
            return new ControlSnapshot(status, workflowExecutionSnapshot, listeners.iterator());
        }
        
        private void completeExecution(WorkflowExecutionSnapshot workflowExecutionSnapshot)
        {
            Validate.validState(workflowExecutionSnapshot.isComplete());
            
            final UUID workflowId = workflow.id();
            logger.info("The workflow {} has completed successfully", workflowId);
            
            // Copy outputs from staging directories into output directory
            
            try {
                copyOutputToTargetDirectory();
            } catch (IOException ex) {
                logger.error("Failed to copy output of workflow {} to target directory", workflowId);
                throw new RuntimeException("Failed to copy output of workflow", ex);
            }
        }
     
        private void invokeAfterNodeListeners(
            Iterable<WorkflowExecutionListener> listeners,
            String nodeName, JobExecution jobExecution, WorkflowExecutionSnapshot workflowExecutionSnapshot)
        {
            for (WorkflowExecutionListener listener: listeners) {
                if (listener instanceof WorkflowExecutionEventListener) {
                    ((WorkflowExecutionEventListener) listener)
                        .afterNode(workflowExecutionSnapshot, nodeName, jobExecution);
                }
            }
        }
        
        private void invokeBeforeNodeListeners(
            Iterable<WorkflowExecutionListener> listeners,
            String nodeName, JobExecution jobExecution, WorkflowExecutionSnapshot workflowExecutionSnapshot)
        {
            for (WorkflowExecutionListener listener: listeners) {
                if (listener instanceof WorkflowExecutionEventListener) {
                    ((WorkflowExecutionEventListener) listener)
                        .beforeNode(workflowExecutionSnapshot, nodeName, jobExecution);
                }
            }
        }
        
        private void invokeSuccessListeners(
            Iterable<WorkflowExecutionListener> listeners, 
            WorkflowExecutionSnapshot workflowExecutionSnapshot)
        {
            for (WorkflowExecutionListener listener: listeners) {
                if (listener instanceof WorkflowExecutionCompletionListener) {
                    ((WorkflowExecutionCompletionListener) listener)
                        .onSuccess(workflowExecutionSnapshot);
                }
            }
        }
        
        private void invokeFailureListeners(
            Iterable<WorkflowExecutionListener> listeners,
            WorkflowExecutionSnapshot workflowExecutionSnapshot)
        {
            for (WorkflowExecutionListener listener: listeners) {
                if (listener instanceof WorkflowExecutionCompletionListener) {
                    ((WorkflowExecutionCompletionListener) listener)
                        .onFailure(workflowExecutionSnapshot);
                }
            }
        }
        
        private void invokeStopListeners(
            Iterable<WorkflowExecutionListener> listeners,
            WorkflowExecutionSnapshot workflowExecutionSnapshot)
        {
            for (WorkflowExecutionListener listener: listeners) {
                if (listener instanceof WorkflowExecutionStopListener) {
                    ((WorkflowExecutionStopListener) listener)
                        .onStopped(workflowExecutionSnapshot);
                }
            }
        }
        
        private void debugFailed(WorkflowExecutionSnapshot workflowExecutionSnapshot)
        {
            Validate.validState(status == ExecutionStatus.FAILED);
            logger.debug(
                "The workflow {} has ceased execution due to failures " +
                    "(failed={}): transition from RUNNING to {}", 
                workflow.id(), failed, status);
        }
        
        private void debugStopped(WorkflowExecutionSnapshot workflowExecutionSnapshot)
        {
            Validate.validState(status == ExecutionStatus.STOPPED);
            logger.debug(
                "The workflow {} has ceased execution due to stop request " +
                    "(stopRequested={}, failed={}): transition from RUNNING to {}", 
                workflow.id(), stopRequested, failed, status);
        }
        
        private void debugFinished()
        {
            Validate.validState(status != ExecutionStatus.RUNNING);
            logger.debug(
                "The workflow {} finished as {} (stopRequested={}, failed={})",
                workflow.id(), status, stopRequested, failed);
        }
        
        private void debugRunning(ExecutionStatus previousStatus)
        {
            Validate.validState(status == ExecutionStatus.RUNNING);
            Validate.validState(!stopRequested && !failed, "Expected flags to be clear");
            
            Map<String,BatchStatus> nodeStatusesByName = workflow.nodeNames().stream()
                .map(nodeName -> Pair.of(nodeName, workflowExecution.node(nodeName).status()))
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
            logger.debug(
                "The workflow {} is now running, moving from {} to RUNNING: execution={}",
                workflow.id(), previousStatus, nodeStatusesByName);
        }
        
        /**
         * Copy output files of a (successfully) completed workflow into output directory.
         * @throws IOException 
         */
        private void copyOutputToTargetDirectory() throws IOException
        {
            Path targetDir = workflow.outputDirectory();
            try { 
                Files.createDirectory(targetDir);
            } catch (FileAlreadyExistsException ex) {
                // no-op: The directory is already there
            }
            
            Map<String,Path> outputMap = workflow.output();
            for (String targetName: outputMap.keySet()) {
                Path source = outputMap.get(targetName);
                Files.createLink(targetDir.resolve(targetName), source);
            }
        }
    }
    
    /**
     * An event listener bound to events of a given workflow execution
     */
    private class ExecutionEventListener implements JobExecutionListener
    {
        private final UUID workflowId;
        
        private final String nodeName;
        
        public ExecutionEventListener(UUID workflowId, String nodeName)
        {
            this.workflowId = workflowId;
            this.nodeName = nodeName;
        }
    
        @Override
        public void beforeJob(JobExecution jobExecution)
        {
            beforeNode(workflowId, nodeName, jobExecution);
        }

        @Override
        public void afterJob(JobExecution jobExecution)
        {
            afterNode(workflowId, nodeName, jobExecution);
        }
    }
    
    private JobRepository jobRepository;
    
    private JobLauncher jobLauncher;
    
    private JobOperator jobOperator;
    
    private JobBuilderFactory jobBuilderFactory;
    
    private StepBuilderFactory stepBuilderFactory;
    
    private long maxDurationAfterUpdate = 1800 * 1000L;
    
    private Map<UUID, ControlBlock> controls = new ConcurrentHashMap<>();
    
    public void setJobBuilderFactory(JobBuilderFactory jobBuilderFactory)
    {
        this.jobBuilderFactory = jobBuilderFactory;
    }
    
    public void setStepBuilderFactory(StepBuilderFactory stepBuilderFactory)
    {
        this.stepBuilderFactory = stepBuilderFactory;
    }
    
    public void setJobRepository(JobRepository jobRepository)
    {
        this.jobRepository = jobRepository;
    }
    
    /**
     * Provide the {@link JobLauncher} to be used to launch a job. An asynchronous launcher
     * is expected here.
     * 
     * @param jobLauncher
     */
    public void setJobLauncher(JobLauncher jobLauncher)
    {
        this.jobLauncher = jobLauncher;
    }
    
    /**
     * Provide the {@link JobOperator} to be used to stop a running execution.
     * @param jobOperator
     */
    public void setJobOperator(JobOperator jobOperator)
    {
        this.jobOperator = jobOperator;
    }
    
    /**
     * Set the duration (since last update) after which a non-running workflow execution
     * may be cleaned up from our map of managed executions.
     * @param duration A duration in seconds
     */
    public void setMaxDurationAfterUpdate(Long duration)
    {
        Validate.isTrue(duration >= 60L);
        this.maxDurationAfterUpdate = duration * 1000L;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception
    {
        Validate.validState(jobRepository != null, "A jobRepository is required");
        Validate.validState(jobLauncher != null, "A jobLauncher is required");
        Validate.validState(jobOperator != null, "A jobOperator is required");
        Validate.validState(jobBuilderFactory != null, "A JobBuilderFactory is required");
        Validate.validState(stepBuilderFactory != null, "A StepBuilderFactory is required");
    }
    
    @Override
    public void start(Workflow workflow, WorkflowExecutionCompletionListener... callbacks) 
        throws WorkflowExecutionStartException
    {        
        Validate.notNull(workflow, "Expected a non-null workflow");
        
        final UUID workflowId = workflow.id();
        
        ControlBlock control = controls
            .computeIfAbsent(workflowId, id -> new ControlBlock(workflow));
        
        try {
            control.startExecution(callbacks);
            logger.info("The workflow {} is started", workflowId);
        } catch (WorkflowExecutionAlreadyRunningException ex) {
            logger.info("The workflow {} is already started", workflowId);
            throw ex;
        } catch (WorkflowExecutionAlreadyCompleteException ex) {
            logger.info("The workflow {} is already complete", workflowId);
            throw ex;
        }
    }

    @Override
    public void stop(UUID workflowId, WorkflowExecutionStopListener... callbacks)
        throws WorkflowExecutionStopException
    {
        Validate.notNull(workflowId, "The workflow ID is required");
        ControlBlock control = controls.get(workflowId);
        
        if (control != null) {
            logger.info("The workflow {} is stopping...", workflowId);
            control.stopExecution(callbacks);
        } else {
            logger.warn("Cannot stop workflow {} because is not started", workflowId);
            throw new WorkflowExecutionNotStartedException();
        }
    }
    
    @Override
    public ExecutionSnapshot poll(UUID workflowId)
    {
        Validate.notNull(workflowId, "The workflow ID is required");
        ControlBlock control = controls.get(workflowId);
        return control != null? control.snapshot() : null;
    }
    
    @Override
    public ExecutionStatus status(UUID workflowId)
    {
        Validate.notNull(workflowId, "The workflow ID is required");
        ControlBlock control = controls.get(workflowId);
        return control != null? control.getStatus() : null;
    }
    
    /**
     * @inheritDoc
     * <p>Note on implementation: The returned set of identifiers will be weakly-consistent,
     * exactly as described in {@link ConcurrentHashMap}.
     */
    @Override
    public Iterable<UUID> list()
    {
        return controls.keySet();
    }
    
    private void afterNode(UUID workflowId, String nodeName, JobExecution jobExecution)
    {
        ControlBlock control = controls.get(workflowId);
        control.afterNode(nodeName, jobExecution);
    }
    
    private void beforeNode(UUID workflowId, String nodeName, JobExecution jobExecution)
    {
        ControlBlock control = controls.get(workflowId);
        control.beforeNode(nodeName, jobExecution);
    }
    
    public void cleanup()
    {
        // Cleanup expired control blocks
        
        logger.info("Checking for expired control blocks...");
        
        long now = System.currentTimeMillis(); // time to check expiration against
        
        int entryCount = 0; // number of examined entries
        List<UUID> expiredKeys = new ArrayList<>();
        for (Map.Entry<UUID, ControlBlock> entry: controls.entrySet()) {
            ControlBlock control = entry.getValue();
            if (control.checkExpired(now)) {
                if (logger.isDebugEnabled()) {
                    Date updated = new Date(control.getLastUpdated());
                    logger.debug(
                        "The control block for workflow {} is expired: status={}, updated={}",
                        entry.getKey(), control.status(), formatDate(updated));
                }
                expiredKeys.add(entry.getKey());
            }
            entryCount++;
        }
        
        if (!expiredKeys.isEmpty()) {
            logger.info("Cleaning up {} (out of {}) control blocks...", 
                expiredKeys.size(), entryCount);
            for (UUID key: expiredKeys)
                controls.remove(key);
        }
    }
}
