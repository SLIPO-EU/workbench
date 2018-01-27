package eu.slipo.workflows.exception;

public class WorkflowExecutionAlreadyCompleteException extends WorkflowExecutionStartException
{
    private static final long serialVersionUID = 1L;

    public WorkflowExecutionAlreadyCompleteException()
    {
        super("The execution is already complete");
    }
}
