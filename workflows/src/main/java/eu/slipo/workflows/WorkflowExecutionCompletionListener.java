package eu.slipo.workflows;

public interface WorkflowExecutionCompletionListener extends WorkflowExecutionListener
{
    void onSuccess(WorkflowExecutionSnapshot workflowExecutionSnapshot);
    
    void onFailure(WorkflowExecutionSnapshot workflowExecutionSnapshot);
}
