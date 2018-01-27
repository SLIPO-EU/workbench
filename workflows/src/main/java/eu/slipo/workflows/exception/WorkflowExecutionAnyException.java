package eu.slipo.workflows.exception;

/**
 * A marker base class for workflow execution-related exceptions
 */
public abstract class WorkflowExecutionAnyException extends Exception
{
    protected WorkflowExecutionAnyException(String message)
    {
        super(message);
    }
    
    protected WorkflowExecutionAnyException(String message, Throwable throwable)
    {
        super(message, throwable);
    }
}
