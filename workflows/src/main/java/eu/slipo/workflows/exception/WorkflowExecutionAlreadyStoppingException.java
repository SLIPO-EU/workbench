package eu.slipo.workflows.exception;

public class WorkflowExecutionAlreadyStoppingException extends WorkflowExecutionStopException
{
    private static final long serialVersionUID = 1L;
    
    public WorkflowExecutionAlreadyStoppingException()
    {
        super("The execution is stopping");
    }
}
