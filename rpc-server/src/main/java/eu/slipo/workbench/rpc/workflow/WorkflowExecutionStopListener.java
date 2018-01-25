package eu.slipo.workbench.rpc.workflow;

@FunctionalInterface
public interface WorkflowExecutionStopListener extends WorkflowExecutionListener
{
    void onStopped(WorkflowExecutionSnapshot workflowExecutionSnapshot);
}
