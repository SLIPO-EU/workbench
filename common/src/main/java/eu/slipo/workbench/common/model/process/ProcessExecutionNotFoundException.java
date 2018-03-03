package eu.slipo.workbench.common.model.process;

public class ProcessExecutionNotFoundException extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public static ProcessExecutionNotFoundException forExecution(long executionId)
    {
        return new ProcessExecutionNotFoundException(executionId);
    }
    
    public static ProcessExecutionNotFoundException forExecutionStep(long executionId, int stepKey)
    {
        return new ProcessExecutionNotFoundException(executionId, stepKey);
    }
    
    private ProcessExecutionNotFoundException(long executionId)
    {
        super(String.format("No process execution entity for execution-id = %d", executionId));
    }
    
    private ProcessExecutionNotFoundException(long executionId, int stepKey)
    {
        super(String.format(
            "No processing step entity of key = %d inside execution of execution-id = %d",
            stepKey, executionId));
    }
}
