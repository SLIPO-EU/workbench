package eu.slipo.workflows.exception;

public class WorkflowExecutionStuckException extends WorkflowExecutionStartException
{
    private static final long serialVersionUID = 1L;

    public WorkflowExecutionStuckException()
    {
        super("The execution is stuck (check for nodes marked as ABANDONED)");
    }
    
    public WorkflowExecutionStuckException(String detail)
    {
        super(String.format("The execution is stuck: %s", detail));
    }
}
