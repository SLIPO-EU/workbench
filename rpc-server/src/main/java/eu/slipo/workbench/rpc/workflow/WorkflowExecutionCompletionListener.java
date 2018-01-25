package eu.slipo.workbench.rpc.workflow;

public interface WorkflowExecutionCompletionListener extends WorkflowExecutionListener
{
    void onSuccess(WorkflowExecutionSnapshot workflowExecutionSnapshot);
    
    void onFailure(WorkflowExecutionSnapshot workflowExecutionSnapshot);
}
