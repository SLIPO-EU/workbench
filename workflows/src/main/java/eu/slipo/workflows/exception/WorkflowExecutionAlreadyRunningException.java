package eu.slipo.workflows.exception;

public class WorkflowExecutionAlreadyRunningException extends WorkflowExecutionStartException
{
    private static final long serialVersionUID = 1L;

    public WorkflowExecutionAlreadyRunningException()
    {
        super("The execution is already running");
    }
}
