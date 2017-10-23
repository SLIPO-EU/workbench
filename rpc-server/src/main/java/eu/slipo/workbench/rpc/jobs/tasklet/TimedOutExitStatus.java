package eu.slipo.workbench.rpc.jobs.tasklet;

import org.springframework.batch.core.ExitStatus;

/**
 * Represent an exit-status of a step execution that failed due to a timeout. 
 * <p>All instances of this class should be regarded as special cases of {@link ExitStatus#FAILED}.
 */
@SuppressWarnings("serial")
public class TimedOutExitStatus extends ExitStatus
{
    /**
     * This exit-code is a special case of a <tt>FAILED</tt> code.
     * <p>Note that we follow Spring-Batch convention of prefixing this code with "FAILED". 
     */
    public static final String EXIT_CODE = "FAILED-WITH-TIMEOUT";
    
    public TimedOutExitStatus(long timeout)
    {
        super(EXIT_CODE, String.format("Timed out at %dms", timeout));
    }
}
