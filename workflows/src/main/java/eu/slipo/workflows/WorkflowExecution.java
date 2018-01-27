package eu.slipo.workflows;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.Validate;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.repository.JobRepository;

/**
 * Represent a workflow execution.
 * 
 * <p>The class is designed to be thread-safe.
 */
public class WorkflowExecution
{
    private static final Set<BatchStatus> RESTARTABLE_STATUSES = 
        EnumSet.of(BatchStatus.UNKNOWN, BatchStatus.STOPPED, BatchStatus.FAILED); 
    
    private static final String UNEXPECTED_TRANSITION_MESSAGE = 
        "Did not expect a batch-status transition from [%s] to [%s]";
    
    private static final String UNEXPECTED_UPDATE_MESSAGE =
        "Did not expect to an update for a batch-status of [%s]";
    
    private static final boolean debug = true;
    
    /**
     * Represent the execution of a single job node ({@link Workflow.JobNode})
     */
    public static class NodeExecution
    {
        private final Workflow.JobNode node;
        
        private final BatchStatus status;
        
        private final long executionId;

        protected NodeExecution(Workflow.JobNode node, BatchStatus status, long executionId)
        {
            this.node = node;
            this.status = status;
            this.executionId = executionId;
        }
        
        public long executionId()
        {
            return executionId;
        }
        
        public Workflow.JobNode node()
        {
            return node;
        }
        
        public String nodeName()
        {
            return node.name();
        }
        
        public String jobName()
        {
            return node.jobName();
        }
        
        public BatchStatus status()
        {
            return status;
        }
        
        protected static NodeExecution of(Workflow.JobNode node, BatchStatus status, long executionId)
        {
            return new NodeExecution(node, status, executionId);
        }
    }
    
    protected final Workflow workflow;
    
    protected final BatchStatus[] statuses;
    
    protected final long[] executionIds;
    
    protected volatile int completeCount = 0; 
    
    protected volatile boolean isAbandoned = false; 
    
    private final BitSet ready;
    
    public WorkflowExecution(Workflow workflow)
    {
        Validate.notNull(workflow, "A workflow is required!");
        
        final int n = workflow.size();
        this.workflow = workflow;
        this.statuses = new BatchStatus[n];
        this.executionIds = new long[n];
        this.ready = new BitSet(n);
        
        Arrays.fill(this.statuses, BatchStatus.UNKNOWN);
        Arrays.fill(this.executionIds, -1L);
    }
   
    /**
     * Load (or reload) status of this workflow execution from the job repository.
     * 
     * @param jobRepository The job repository
     * @param fixTransientStatuses A flag to indicate if transient statuses should be allowed.
     *   If true, then a transient batch status (i.e {@link BatchStatus#STOPPING} and 
     *   {@link BatchStatus#STARTING}) will be mapped to its target status (i.e. 
     *   {@link BatchStatus#STOPPED} and {@link BatchStatus#STARTED} respectively).
     *   If false, then a batch status is loaded exactly as found in repository.
     */
    public void load(JobRepository jobRepository, boolean fixTransientStatuses)
    {
        load1(jobRepository, fixTransientStatuses);
    }
    
    /**
     * Load (or reload) status of this workflow execution from the job repository.
     * 
     * @param jobRepository The job repository
     */
    public void load(JobRepository jobRepository)
    {
        load1(jobRepository, false);
    }
    
    private synchronized void load1(JobRepository jobRepository, boolean fixTransientStatuses)
    {
        final int n = workflow.size();
        
        // Fetch job executions (if any) from job repository
        
        completeCount = 0;
        isAbandoned = false;
        
        for (int vertex = 0; vertex < n; ++vertex) {
            final Workflow.JobNode node = workflow.node(vertex); 
            JobExecution jobExecution = jobRepository
                .getLastJobExecution(node.jobName(), node.parameters());
            if (jobExecution == null) {
                statuses[vertex] = BatchStatus.UNKNOWN;
                executionIds[vertex] = -1L;
            } else {
                BatchStatus status = jobExecution.getStatus();
                // Examine (and maybe re-map) status, update flags and counters
                switch (status) {
                case COMPLETED:
                    completeCount++;
                    break;
                case ABANDONED:
                    isAbandoned = true;
                    break;
                case STOPPING:
                    if (fixTransientStatuses)
                        status = BatchStatus.STOPPED;
                    break;
                case STARTING:
                    if (fixTransientStatuses)
                        status = BatchStatus.STARTED;
                    break;
                default:
                    break;
                }
                // Populate vertex with status and execution-id
                statuses[vertex] = status;
                executionIds[vertex] = jobExecution.getId();
            }
        }
        
        // Compute ready set
        
        for (int vertex = 0; vertex < n; ++vertex)
            ready.set(vertex, checkReady(vertex));
    }
    
    private boolean checkReady(int vertex)
    {
        // Check status
        
        if (!RESTARTABLE_STATUSES.contains(statuses[vertex]))
            return false;
        
        // The status is restartable: Check if dependencies are all complete
        
        Workflow.JobNode node = workflow.node(vertex);
        for (Workflow.JobNode dependency: node.dependencies()) {
            int u = dependency.vertex();
            if (statuses[u] != BatchStatus.COMPLETED)
                return false;
        }
        
        return true;
    }
    
    /**
     * Check if execution is complete (i.e. every node is complete)
     */
    public boolean isComplete()
    {
        return completeCount == statuses.length;
    }
    
    /**
     * Check if execution is failed (i.e. at least one node is failed).
     * 
     * <p>Note that a failed execution does not necessarily mean that execution 
     * of all nodes is ceased (other nodes may still be running).
     */
    public synchronized boolean isFailed()
    {
        for (BatchStatus status: statuses)
            if (status == BatchStatus.FAILED)
                return true;
        return false;
    }
    
    /**
     * Check if execution is running i.e at least one one is running
     */
    public synchronized boolean isRunning()
    {
        for (BatchStatus status: statuses)
            if (status.isRunning())
                return true;
        return false;
    }
    
    /**
     * Check if execution is abandoned (i.e at least one node is abandoned).
     * 
     * <p>Note that an abandoned workflow execution is considered stuck, because
     * an abandoned node represents a non-restartable job instance
     */
    public boolean isAbandoned()
    {
        return isAbandoned;
    }
    
    public int countCompleted()
    {
        return completeCount;
    }
    
    public int countRunning()
    {
        return countNodes(BatchStatus::isRunning);
    }
    
    public int countFailed()
    {
        return countNodes(BatchStatus::isUnsuccessful);
    }
    
    private synchronized int countNodes(Predicate<BatchStatus> pred)
    {
        int count = 0;
        for (BatchStatus status: statuses)
            if (pred.test(status))
                count++;
        return count;
    }
    
    /**
     * Update status of this workflow execution on a new event on a job node.
     * 
     * @param nodeName The name for a job node
     * @param jobExecution The Batch execution for the named job node
     * @return a list of nodes that became ready during this update. Note that, since an
     *   update is synchronized, a node can become ready only once (when the last dependency
     *   is complete). A scheduler may use this fact to pick up a set of nodes to be 
     *   (re)started after an update.
     */
    public List<Workflow.JobNode> update(String nodeName, JobExecution jobExecution)
    {                
        BitSet readyTrack = new BitSet(ready.size());
        update(workflow.node(nodeName), jobExecution, readyTrack);
        
        return readyTrack.cardinality() == 0? 
            Collections.emptyList() : 
            Collections.unmodifiableList(
                readyTrack.stream().mapToObj(v -> workflow.node(v))
                    .collect(Collectors.toList()));
    }
    
    private synchronized void update(Workflow.JobNode node, JobExecution jobExecution, BitSet readyTrack)
    {        
        final BatchStatus status = jobExecution.getStatus();
        final int vertex = node.vertex();
        
        // Update status for job node
        
        final BatchStatus prevStatus = statuses[vertex];
        statuses[vertex] = status;
        executionIds[vertex] = jobExecution.getId(); 
        
        // Update ready set
        
        switch (status) {
        case COMPLETED:
            {
                Validate.validState(!ready.get(vertex), 
                    "Did not expect a completed node to be ready");
                Validate.validState(prevStatus == BatchStatus.STARTED, 
                    UNEXPECTED_TRANSITION_MESSAGE, prevStatus, status);
                completeCount++;
                // Check dependents; only those can become ready
                for (Workflow.JobNode dependent: node.dependents()) {
                    int u = dependent.vertex();
                    Validate.validState(!ready.get(u), 
                        "Did not expect a dependent node to be ready (multiple updates on same event?)!");
                    if (checkReady(u)) {
                        ready.set(u);
                        readyTrack.set(u);
                    }
                }
            }
            break;
        case STARTED:
            {
                if (prevStatus.isRunning()) {
                    Validate.validState(!ready.get(vertex), 
                        "Did not expect a running node to be ready");
                } else {
                    Validate.validState(ready.get(vertex), 
                        "Expected a started node to be ready");
                    Validate.validState(RESTARTABLE_STATUSES.contains(prevStatus),
                        UNEXPECTED_TRANSITION_MESSAGE, prevStatus, status);
                    ready.clear(vertex);
                }
            }
            break;
        case STARTING:
            {
                Validate.validState(ready.get(vertex), 
                    "Expected a starting node to be ready");
                Validate.validState(RESTARTABLE_STATUSES.contains(prevStatus),
                    UNEXPECTED_TRANSITION_MESSAGE, prevStatus, status);
                ready.clear(vertex);
            }
            break;
        case STOPPED:
            {
                Validate.validState(!ready.get(vertex), 
                    "Did not expect a stopped node to be ready");
                Validate.validState(prevStatus == BatchStatus.STARTED || prevStatus == BatchStatus.STOPPING, 
                    UNEXPECTED_TRANSITION_MESSAGE, prevStatus, status);
                ready.set(vertex);
                readyTrack.set(vertex);
            }
            break;
        case FAILED:
            {
                Validate.validState(!ready.get(vertex), 
                    "Did not expect a failed node to be ready");
                Validate.validState(prevStatus == BatchStatus.STARTED, 
                    UNEXPECTED_TRANSITION_MESSAGE, prevStatus, status);
                ready.set(vertex);
                readyTrack.set(vertex);
            }
            break;
        case STOPPING:
            {
                Validate.validState(!ready.get(vertex), "Did not expect a stopping node to be ready");
                Validate.validState(prevStatus.isRunning(), 
                    UNEXPECTED_TRANSITION_MESSAGE, prevStatus, status);
                // no-op: update only when STOPPED, in the meanwhile consider as running 
            }
            break;
        case ABANDONED:
        case UNKNOWN:  
        default:
            Validate.validState(false, String.format(UNEXPECTED_UPDATE_MESSAGE, status));
            break;
        }
        
        //// Fixme remove debugGraph
        //if (debug) System.err.println(debugGraph());
    }
    
    /**
     * Get names of job nodes
     */
    public Set<String> nodeNames()
    {
        return workflow.nodeNames();
    }
    
    /**
     * Iterate on the subset of <em>ready</em> nodes. 
     * 
     * <p>A node is considered <em>ready</em> if: <ul>
     *    <li>the corresponding job is in a (re)startable state, i.e is {@link BatchStatus#UNKNOWN} or
     *      {@link BatchStatus#FAILED} or {@link BatchStatus#STOPPED}. 
     *    <li>all its dependencies are resolved (i.e. corresponding jobs are complete)
     * </ul>
     * 
     * <p>Note that reported statuses ({@link BatchStatus}) are a snapshot of statuses at the time 
     * this method is invoked. 
     */
    public synchronized Iterable<NodeExecution> readyNodes()
    {
        final int n = workflow.size();
        
        final BatchStatus[] statusesSnapshot = Arrays.copyOf(statuses, n);
        final long[] xidsSnapshot = Arrays.copyOf(executionIds, n);
        final BitSet selected = BitSet.valueOf(ready.toLongArray());
        
        return () -> selected.stream()
            .mapToObj(v -> 
                NodeExecution.of(workflow.node(v), statusesSnapshot[v], xidsSnapshot[v]))
            .iterator();
    }
    
    /**
     * Iterate on the subset of nodes at a given status ({@link BatchStatus})
     */
    public synchronized Iterable<NodeExecution> nodes(BatchStatus status)
    {
        Validate.notNull(status, "A batch status is required");
        
        final int n = workflow.size();
        final long[] xidsSnapshot = Arrays.copyOf(executionIds, n);
        
        final BitSet selected = new BitSet(n);
        for (int vertex = 0; vertex < n; ++vertex)
            if (statuses[vertex] == status)
                selected.set(vertex);
        
        return () -> selected.stream()
            .mapToObj(v -> 
                NodeExecution.of(workflow.node(v), status, xidsSnapshot[v]))
            .iterator();
    }
    
    /**
     * Iterate on the entire set of job nodes. 
     * 
     * <p>Note that reported statuses ({@link BatchStatus}) are a snapshot of statuses at the time 
     * this method is invoked. 
     */
    public synchronized Iterable<NodeExecution> nodes()
    {
        final int n = workflow.size();
        final long[] xidsSnapshot = Arrays.copyOf(executionIds, n);
        final BatchStatus[] statusesSnapshot = Arrays.copyOf(statuses, n);
        
        return () -> IntStream.range(0, n)
            .mapToObj(v -> 
                NodeExecution.of(workflow.node(v), statusesSnapshot[v], xidsSnapshot[v]))
            .iterator();
    }
    
    /**
     * Get a job node along with its status.
     * 
     * @param nodeName
     */
    public synchronized NodeExecution node(String nodeName)
    {
        Workflow.JobNode node = workflow.node(nodeName);
        int vertex = node.vertex();
        return NodeExecution.of(node, statuses[vertex], executionIds[vertex]);
    }
    
    /**
     * Get the underlying workflow
     */
    public Workflow workflow()
    {
        return workflow;
    }
    
    /**
     * Take a snapshot of this workflow execution
     */
    public synchronized WorkflowExecutionSnapshot snapshot()
    {
        return new WorkflowExecutionSnapshot(this);
    }
}
