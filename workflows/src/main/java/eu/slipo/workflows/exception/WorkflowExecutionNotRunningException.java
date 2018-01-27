package eu.slipo.workflows.exception;

public class WorkflowExecutionNotRunningException extends WorkflowExecutionStopException
{
    private static final long serialVersionUID = 1L;
    
    public WorkflowExecutionNotRunningException()
    {
        super("The execution is not running");
    }
}
