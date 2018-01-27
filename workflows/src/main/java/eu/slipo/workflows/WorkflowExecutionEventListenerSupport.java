package eu.slipo.workflows;

import org.springframework.batch.core.JobExecution;

public class WorkflowExecutionEventListenerSupport 
    extends WorkflowExecutionCompletionListenerSupport implements WorkflowExecutionEventListener 
{
    @Override
    public void beforeNode(
        WorkflowExecutionSnapshot workflowExecutionSnapshot, String nodeName, JobExecution jobExecution)
    {
        //  no-op
    }

    @Override
    public void afterNode(
        WorkflowExecutionSnapshot workflowExecutionSnapshot, String nodeName, JobExecution jobExecution)
    {
        // no-op
    }

}
