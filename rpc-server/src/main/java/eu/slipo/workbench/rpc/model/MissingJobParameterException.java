package eu.slipo.workbench.rpc.model;

import org.springframework.batch.core.JobExecutionException;

import eu.slipo.workbench.rpc.service.JobService;

/**
 * A checked exception to signal a missing job parameter: no value is supplied and 
 * no default value exists.
 * <p>
 * Note that this exception is not part of the {@link JobExecutionException} hierarchy,
 * because it is never thrown during job execution: it can only be thrown during 
 * preparation of job parameters (happening just before the job is launched).
 * 
 * @see {@link JobService#prepareParameters(String, java.util.Map)}
 */
@SuppressWarnings("serial")
public class MissingJobParameterException extends Exception
{
    public MissingJobParameterException(String message)
    {
        super(message);
    }
}
