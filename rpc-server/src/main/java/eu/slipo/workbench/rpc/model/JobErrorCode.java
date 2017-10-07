package eu.slipo.workbench.rpc.model;

import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;

import eu.slipo.workbench.common.model.ErrorCode;

/**
 * An (application-level) error code corresponding to job-related exceptions.
 */
public enum JobErrorCode implements ErrorCode
{
    JOB_NOT_FOUND,
    
    JOB_EXECUTION_ALREADY_RUNNING,
    JOB_CANNOT_RESTART,
    JOB_INSTANCE_ALREADY_COMPLETE,
    
    JOB_INVALID_PARAMETERS,
    JOB_MISSING_PARAMETER,
    
    ;

    /**
     * Map an instance of {@link Exception} to an error code.
     * 
     * @param ex
     * @return an instance of {@link JobErrorCode}, or <tt>null</tt> if no
     *   suitable mapping exists.
     */
    public static JobErrorCode fromException(Exception ex)
    {
        JobErrorCode r = null;
        
        if (ex instanceof JobExecutionAlreadyRunningException)
            r = JOB_EXECUTION_ALREADY_RUNNING;
        else if (ex instanceof JobRestartException)
            r = JOB_CANNOT_RESTART;
        else if (ex instanceof JobInstanceAlreadyCompleteException)
            r = JOB_INSTANCE_ALREADY_COMPLETE;
        else if (ex instanceof JobParametersInvalidException)
            r = JOB_INVALID_PARAMETERS;
        else if (ex instanceof MissingJobParameterException)
            r = JOB_MISSING_PARAMETER;
        else if (ex instanceof NoSuchJobException)
            r = JOB_NOT_FOUND;
        
        return r;
    }
    
    @Override
    public String key()
    {
        return (this.getClass().getSimpleName() + '.' + name());
    }
}
