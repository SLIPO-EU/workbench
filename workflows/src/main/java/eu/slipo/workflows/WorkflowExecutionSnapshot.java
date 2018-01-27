package eu.slipo.workflows;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import org.springframework.batch.core.BatchStatus;

import eu.slipo.workflows.util.digraph.ExportDependencyGraph;


/**
 * Represent a snapshot of a workflow execution.
 */
public class WorkflowExecutionSnapshot
{
    private final Workflow workflow;
    
    private final BatchStatus[] statuses;
    
    private final long[] executionIds;
    
    private final int completeCount;
    
    protected WorkflowExecutionSnapshot(WorkflowExecution workflowExecution)
    {
        this.workflow = workflowExecution.workflow();        
        final int n = this.workflow.size();
        
        this.statuses = Arrays.copyOf(workflowExecution.statuses, n);
        this.executionIds = Arrays.copyOf(workflowExecution.executionIds, n);
        this.completeCount = workflowExecution.completeCount;
    }
    
    public Workflow workflow()
    {
        return workflow;
    }
    
    public boolean isComplete()
    {
        return completeCount == statuses.length;
    }
    
    public boolean isFailed()
    {
        return Arrays.stream(statuses).anyMatch(status -> status == BatchStatus.FAILED);
    }
    
    public boolean isRunning()
    {
        return Arrays.stream(statuses).anyMatch(BatchStatus::isRunning);
    }
    
    public Set<String> nodeNames()
    {
        return workflow.nodeNames();
    }
    
    public Iterable<WorkflowExecution.NodeExecution> nodes()
    {
        final int n = workflow.size();
        return () -> IntStream.range(0, n)
            .mapToObj(vertex -> new WorkflowExecution.NodeExecution(
                workflow.node(vertex), statuses[vertex], executionIds[vertex]))
            .iterator();
    }
    
    public WorkflowExecution.NodeExecution node(String nodeName)
    {
        Workflow.JobNode node = workflow.node(nodeName);
        int vertex = node.vertex();
        return new WorkflowExecution.NodeExecution(node, statuses[vertex], executionIds[vertex]);
    }
    
    public BatchStatus status(String nodeName)
    {
        Workflow.JobNode node = workflow.node(nodeName);
        int vertex = node.vertex();
        return statuses[vertex];
    }
    
    public long executionId(String nodeName)
    {
        Workflow.JobNode node = workflow.node(nodeName);
        int vertex = node.vertex();
        return executionIds[vertex];
    }
    
    //
    // Debug helpers
    //
    
    private static final Map<BatchStatus,String> COLOR_MAP = new EnumMap<>(BatchStatus.class);
    static {
        COLOR_MAP.put(BatchStatus.UNKNOWN, "white");
        COLOR_MAP.put(BatchStatus.STARTED, "green");
        COLOR_MAP.put(BatchStatus.STARTING, "green");
        COLOR_MAP.put(BatchStatus.FAILED, "red");
        COLOR_MAP.put(BatchStatus.ABANDONED, "cadetblue");
        COLOR_MAP.put(BatchStatus.COMPLETED, "grey80");
        COLOR_MAP.put(BatchStatus.STOPPED, "orange");
        COLOR_MAP.put(BatchStatus.STOPPING, "orange");
    }
    
    public synchronized String debugGraph()
    {
        final WorkflowExecutionSnapshot workflowExecutionSnapshot = this;
        IntFunction<ExportDependencyGraph.NodeAttributes> styleMapper = 
            (v) -> workflowExecutionSnapshot.styleNode(v);
        return workflow.debugGraph(styleMapper);
    }
    
    private ExportDependencyGraph.NodeAttributes styleNode(int vertex)
    {
        BatchStatus status = statuses[vertex];
        ExportDependencyGraph.NodeAttributes attrs = new ExportDependencyGraph.NodeAttributes();
        attrs.setFillColor(COLOR_MAP.get(status));
        return attrs;
    }
    
}
