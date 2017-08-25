package eu.slipo.workbench.rpc.service;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.JobFactory;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobInstanceAlreadyExistsException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SimpleJobService implements JobService
{
    private static final Logger logger = LoggerFactory.getLogger(SimpleJobService.class);
    
    @Autowired
    JobExplorer explorer;
    
    @Autowired
    JobRepository repository;
    
    @Autowired
    JobRegistry registry;
    
    @Autowired
    JobLauncher launcher;
    
    @Override
    public JobExecution start(String jobName, JobParameters parameters)
    {        
        JobExecution x = null;
        Job job = null;
        
        try {
            job = registry.getJob(jobName);
        } catch (NoSuchJobException ex) {
            job = null;
            logger.error("No such job: {}", jobName);
        }
        
        if (job != null) {
            try {
                x = launcher.run(job, parameters);
            } catch (Exception e) {
                x = null;
                logger.error("Failed to start job: {}", e.getMessage());
            }
        }
        
        return x;
    }
    
    /**
     * This method is implemented in a similar (but more simplified) way to 
     * {@link SimpleJobOperator#stop(long)}.
     * <p>
     * Note: A step defined as tasklet extending {@link StoppableTasklet} will be ignored (no 
     * callback is invoked). 
     * 
     * @see JobService#stop(JobExecution)
     */
    @Override
    public void stop(long executionId)
    {
        JobExecution execution = findExecution(executionId);
        
        // The execution should be stopped by setting it's status to STOPPING. It is 
        // assumed that the step implementation will check this status at chunk boundaries.
        
        BatchStatus status = execution.getStatus();
        if (!(status == BatchStatus.STARTED || status == BatchStatus.STARTING)) {
            logger.info(
                "The job execution #{} cannot be stopped because is not running ({})",
                executionId, status);
            return;
        } 
        
        execution.setStatus(BatchStatus.STOPPING);
        repository.update(execution);
    }

    @Override
    public void stop(String jobName, JobParameters params)
    {
      JobInstance y = findInstance(jobName, params);
      if (y != null) {
          long yid = y.getInstanceId();
          JobExecution x = findRunningExecution(jobName, yid);
          if (x != null) {
              stop(x.getId());
          } else {
              logger.info("No running execution for job instance #{}", yid);
          }
      } else {
          logger.warn("No job instance for name={} parameters={}", jobName, params);
      }
    }
    
    @Override
    public List<JobExecution> findExecutions(String jobName, long instanceId)
    {
        return explorer.getJobExecutions(new JobInstance(instanceId, jobName));
    }

    @Override
    public JobInstance findInstance(String jobName, JobParameters params)
    {
        // Note: The above is not always correct: a JobInstance could have been 
        // created but not started (thus, no executions will be found in repository). 
        // However, if always launching jobs via JobLauncher, a launched Job will 
        // always have at least one associated execution.
        
        JobExecution x = repository.getLastJobExecution(jobName, params);
        return x != null? x.getJobInstance() : null;
    }

    @Override
    public Set<JobExecution> findRunningExecutions(String jobName)
    {
        return explorer.findRunningJobExecutions(jobName);
    }

    @Override
    public JobExecution findRunningExecution(String jobName, long instanceId)
    {
        List<JobExecution> executions = 
            explorer.getJobExecutions(new JobInstance(instanceId, jobName));
            
        for (JobExecution x: executions)
            if (x.isRunning())
                return x;
        return null;
    }

    @Override
    public List<JobInstance> findInstances(String jobName, int start, int count)
    {
        return explorer.findJobInstancesByJobName(jobName, start, count);
    }

    @Override
    public int countInstances(String jobName)
    {
        int n = 0;
        try {
            n = explorer.getJobInstanceCount(jobName);
        } catch (NoSuchJobException e) {
            n = -1;
        }
        return n;
    }

    @Override
    public JobInstance findInstance(long instanceId)
    {
        return explorer.getJobInstance(instanceId);
    }

    @Override
    public JobExecution findExecution(long executionId)
    {
        return explorer.getJobExecution(executionId);
    }
    
    @Override
    public JobExecution clearRunningExecution(String jobName, long instanceId)
    {
        JobExecution x = findRunningExecution(jobName, instanceId);
        if (x != null) {
            logger.info(
                "About to clear running execution #{} for instance #{}", 
                x.getId(), instanceId);
            // Update record for this job execution
            x.setStatus(BatchStatus.STOPPED);
            x.setExitStatus(ExitStatus.UNKNOWN);
            x.setEndTime(x.getCreateTime()); // must have non-null endTime!
            repository.update(x);
            return x;
        }
        return null;
    }
}