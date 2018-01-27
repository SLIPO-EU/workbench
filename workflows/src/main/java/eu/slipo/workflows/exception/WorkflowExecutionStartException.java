package eu.slipo.workflows.exception;

public class WorkflowExecutionStartException extends WorkflowExecutionAnyException
{
    private static final long serialVersionUID = 1L;

    public WorkflowExecutionStartException(String message)
    {
        super(message);
    }
    
    public WorkflowExecutionStartException(String message, Throwable throwable)
    {
        super(message, throwable);
    }
}
