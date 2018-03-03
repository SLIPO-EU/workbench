package eu.slipo.workbench.common.model.process;

/**
 * An exception representing an operator-level failure while trying to stop a process execution.
 * 
 * A more specific stop-related exception should subclass this generic exception.
 */
public class ProcessExecutionStopException extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public ProcessExecutionStopException(String message, Throwable ex)
    {
        super(message, ex);
    }

    public ProcessExecutionStopException(Throwable ex)
    {
        super(ex);
    }
    
    public ProcessExecutionStopException(String message)
    {
        super(message);
    }
}
