package eu.slipo.workflows;

import org.springframework.batch.core.JobExecution;

public interface WorkflowExecutionEventListener extends WorkflowExecutionCompletionListener
{
    void beforeNode(
        WorkflowExecutionSnapshot snapshot, String nodeName, JobExecution jobExecution);
    
    void afterNode(
        WorkflowExecutionSnapshot snapshot, String nodeName, JobExecution jobExecution);
}
