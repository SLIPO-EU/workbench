package eu.slipo.workbench.rpc.service;

import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.NoSuchJobException;

import eu.slipo.workbench.rpc.model.MissingJobParameterException;

/**
 * A facade interface to interact with Spring Batch jobs.
 */
public interface JobService
{
    /**
     * List names of registered jobs.
     */
    Collection<String> getNames();
    
    /**
     * Start (or restart) a job, passing a map of parameters. 
     * 
     * @param job
     * @param parameters
     * @return the started {@link JobExecution}
     * @throws JobExecutionException if the job could not be started (or restarted).
     */
    JobExecution start(Job job, JobParameters parameters) throws JobExecutionException;
    
    /**
     * Start (or restart) a job, passing a map of parameters.
     * 
     * @param jobName The name of a registered job
     * @param parameters
     * @return the started job execution
     * @throws JobExecutionException if the job could not be started (or restarted).
     */
    JobExecution start(String jobName, JobParameters parameters) throws JobExecutionException;
    
    /**
     * Run (or re-run) a job synchronously, passing a map of parameters.
     * 
     * @param job
     * @param parameters
     * @return the resolved {@link JobExecution}
     * @throws JobExecutionException if the job could not be started (or restarted).
     */
    JobExecution run(Job job, JobParameters parameters) throws JobExecutionException;
    
    /**
     * Run (or re-run) a job synchronously, passing a map of parameters.
     * 
     * @param jobName The name of a registered job
     * @param parameters
     * @return the resolved {@link JobExecution}
     * @throws JobExecutionException if the job could not be started (or restarted).
     */
    JobExecution run(String jobName, JobParameters parameters) throws JobExecutionException;
    
    /**
     * Stop a running job execution ({@link JobExecution}). If given execution does
     * not represent a running execution, it will do nothing.
     * 
     * @param executionId the id of target job execution
     * @return the updated {@link JobExecution} object
     */
    void stop(long executionId);
    
    /**
     * Stop a running job execution ({@link JobExecution}). The target execution is
     * found as the running execution of a job instance ({@link JobInstance}) identified
     * by job-name and parameters.
     * 
     * @param jobName
     * @param params
     */
    void stop(String jobName, JobParameters params);
    
    /**
     * Find a {@link JobInstance} identified by its instance id. 
     * If nothing is found, <tt>null</tt> is returned. 
     * 
     * @param instanceId
     */
    JobInstance findInstance(long instanceId);
    
    /**
     * Find a {@link JobInstance} identified by a a pair of (jobName, parameters). 
     * If nothing is found, <tt>null</tt> is returned.
     * 
     * @param jobName
     * @param params
     */
    JobInstance findInstance(String jobName, JobParameters params); 
    
    /**
     * Find list of {@link JobInstance} for a given job name. 
     * 
     * <p>The number of instances can be quite large, so you should only fetch them in pages. 
     * 
     * @param jobName
     * @param start The index to start returning from
     * @param count The page size
     */
    List<JobInstance> findInstances(String jobName, int start, int count);
    
    /**
     * Fetch entire list of {@link JobInstance} for a given job name.
     * 
     * @param jobName
     */
    default List<JobInstance> findInstances(String jobName)
    {
        int n = countInstances(jobName);
        return n > 0? findInstances(jobName, 0, n) : Collections.emptyList();
    }
    
    /**
     * Count the number of created job instances ({@link JobInstance}) for a given job name.
     * 
     * @param jobName
     * @return the actual count, or -1 if no job with given name exists.
     */
    int countInstances(String jobName);
    
    /**
     * Find a {@link JobExecution} identified by its execution id.
     * If nothing is found, <tt>null</tt> is returned. 
     * 
     * @param executionId
     */
    JobExecution findExecution(long executionId);
    
    /**
     * Find list of {@link JobExecution} for a given job instance ({@link JobInstance}) 
     * which is identified by (jobName, instanceId).
     * 
     * <p>
     * A {@link JobInstance} may be associated with more than one executions (i.e {@link JobExecution})
     * if failed and restarted.
     *
     * @param instanceId
     * @see http://docs.spring.io/spring-batch/reference/html/domain.html
     */
    List<JobExecution> findExecutions(long instanceId);
    
    /**
     * Find list of {@link JobExecution} for a job instance identified by a pair of (jobName, parameters).
     * 
     * @param jobName
     * @param params
     * @see http://docs.spring.io/spring-batch/reference/html/domain.html
     */
    default List<JobExecution> findExecutions(String jobName, JobParameters params)
    {
        JobInstance y = findInstance(jobName, params);
        return y != null? findExecutions(y.getInstanceId()): Collections.emptyList();    
    }
   
    /**
     * Find set of running executions ({@link JobExecution}) for a given job name. For each job
     * instance there will be at most one running execution.
     * 
     * @param jobName
     * @return
     */
    Set<JobExecution> findRunningExecutions(String jobName);
    
    /**
     * Find running execution ({@link JobExecution}) for a given job instance ({@link JobInstance}). 
     * At any time, at most one running execution exists per job instance.
     * 
     * @param instanceId
     * @return a {@link JobExecution} or <tt>null</tt> if no running execution exists
     */
    JobExecution findRunningExecution(long instanceId);
    
    /**
     * Find running execution ({@link JobExecution}) for a job instance identified by a pair
     * of (jobName, parameters).
     * 
     * @param jobName
     * @param params
     * @return a {@link JobExecution} or <tt>null</tt> if no running execution exists
     */
    JobExecution findRunningExecution(String jobName, JobParameters params);
    
    /**
     * Find last execution ({@link JobExecution}) for a job instance identified by a pair
     * of (jobName, parameters). 
     * 
     * <p>Because executions of a job instance will never overlap in time, the <em>last</em> execution
     * (if any) is equivalent to the one started most recently.
     * 
     * @param jobName
     * @param params
     * @return a {@link JobExecution} or <tt>null</tt> if no execution exists
     */
    JobExecution findLastExecution(String jobName, JobParameters params);
    
    /**
     * Reset a running execution of a given job instance to a non-running status.
     * 
     * <p>Note: This method does not attempt to stop a running task: only resets its status.
     * Its purpose is to clear the status of an interrupted task (e.g. by the application
     * shutdown) that falsely appears as {@link BatchStatus#STARTED} but in fact is stopped 
     * in an unknown state. This recovery step is needed because Spring Batch will refuse 
     * to restart the same job instance thinking it already has an active execution.  
     *
     * @param instanceId
     * @param newStatus the new status that a running execution should be set to (must 
     *   be a non-running status)
     *   
     * @return a {@link JobExecution} instance if cleared, <tt>null</tt> otherwise
     */
    JobExecution clearRunningExecution(long instanceId, BatchStatus newStatus);
    
    default JobExecution clearRunningExecution(long instanceId) 
    {
        return clearRunningExecution(instanceId, BatchStatus.ABANDONED);
    }
    
    /**
     * Prepare job parameters for a given job: load defaults, merge runtime parameters into defaults, 
     * convert to proper data types (if needed).
     * <p>
     * If a parameter acquires its default value by evaluating an expression, this evaluation should
     * take place here.
     * 
     * @param providedParameters A map of runtime parameters
     * @param jobName The job name
     * 
     * @throws MissingJobParameterException
     * @throws NumberFormatException
     * @throws DateTimeParseException
     * 
     * @return a new {@link JobParameters} instance
     */
    JobParameters prepareParameters(String jobName, Map<String,Object> providedParameters)
        throws MissingJobParameterException;
    
    default JobParameters prepareParameters(String jobName)
        throws MissingJobParameterException
    {
        return prepareParameters(jobName, Collections.emptyMap());
    }
}