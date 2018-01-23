package eu.slipo.workbench.rpc.workflow.exception;

public class WorkflowExecutionStopException extends WorkflowExecutionAnyException
{
    private static final long serialVersionUID = 1L;

    public WorkflowExecutionStopException(String message)
    {
        super(message);
    }
    
    public WorkflowExecutionStopException(String message, Throwable throwable)
    {
        super(message, throwable);
    }
}
