package eu.slipo.workflows;

@FunctionalInterface
public interface WorkflowExecutionStopListener extends WorkflowExecutionListener
{
    void onStopped(WorkflowExecutionSnapshot workflowExecutionSnapshot);
}
