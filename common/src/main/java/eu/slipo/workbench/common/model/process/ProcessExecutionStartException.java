package eu.slipo.workbench.common.model.process;

/**
 * An exception representing a failure while trying to start a process execution.
 * 
 * A more specific start-related exception should subclass this generic exception.    
 */
public class ProcessExecutionStartException extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public ProcessExecutionStartException(Throwable ex, String message)
    {
        super(message, ex);
    }

    public ProcessExecutionStartException(Throwable ex)
    {
        super(ex);
    }
    
    public ProcessExecutionStartException(String message)
    {
        super(message);
    }
    
    public ProcessExecutionStartException(String message, Throwable ex)
    {
        super(message, ex);
    }
}
