package eu.slipo.workbench.rpc.workflow.exception;

public class WorkflowExecutionAlreadyRunningException extends WorkflowExecutionStartException
{
    private static final long serialVersionUID = 1L;

    public WorkflowExecutionAlreadyRunningException()
    {
        super("The execution is already running");
    }
}
