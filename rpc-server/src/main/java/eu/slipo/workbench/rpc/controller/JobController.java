package eu.slipo.workbench.rpc.controller;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;

import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.model.jobs.JobExecutionInfo;
import eu.slipo.workbench.common.model.jobs.JobInstanceInfo;
import eu.slipo.workbench.common.model.ErrorCode;
import eu.slipo.workbench.rpc.model.JobExecutionErrorCode;
import eu.slipo.workbench.rpc.service.JobService;

@RestController
@RequestMapping(produces = "application/json")
public class JobController
{
    private static final Logger logger = LoggerFactory.getLogger(JobController.class);
    
    @Autowired
    JobService jobService;
    
    /**
     * Create a DTO object ({@link JobExecutionInfo}) from a job execution ({@link JobExecution}).
     */
    private static JobExecutionInfo createExecutionInfo(JobExecution execution)
    {
        JobExecutionInfo r = new JobExecutionInfo(); 
        if (execution != null) {
            r.setId(execution.getJobId());
            r.setExecutionId(execution.getId());
            r.setStarted(execution.getStartTime());
            r.setFinished(execution.getEndTime());
        }
        return r;
    }
    
    private static JobInstanceInfo createInstanceInfo(JobInstance instance)
    {
        JobInstanceInfo r = new JobInstanceInfo(instance.getJobName(), instance.getId());
        return r;
    }
    
    /**
     * List registered job names.
     */
    @GetMapping(value = "/api/jobs")
    public RestResponse<Collection<String>> listNames()
    {
        return RestResponse.result(jobService.getNames());
    }
    
    /**
     * Submit a new job.
     * <p>
     * Note that this may or may not create a new job instance (since a previously stopped/failed
     * job instance may be restarted).
     * 
     * @param jobName
     * @param foo Fixme remove foo parameter
     */
    @PostMapping(value = "/api/jobs/{jobName}/submit", consumes = "application/json")
    public RestResponse<JobExecutionInfo> submit(@PathVariable String jobName, @RequestParam("foo") String foo)
    {
        // Todo Load default (per job) parameters from database or properties file
        // Todo Load request-specific job parameters from request body
        
        // Prepare parameters for a job
        
        Map<String, JobParameter> parameters = new HashMap<>();
        parameters.put("boo", new JobParameter(199L));
        parameters.put("foo", new JobParameter(foo));
        
        // Submit job to job service
        
        logger.info("Starting job {} with parameters {}", jobName, parameters);
        
        JobExecution execution = null;
        String errorMessage = null;
        ErrorCode errorCode = null;
        try {
            execution = jobService.start(jobName, new JobParameters(parameters));
            logger.info("Started job as: {}", execution);
        } catch (JobExecutionException ex) {
            execution = null;
            errorMessage = ex.getMessage();
            errorCode = JobExecutionErrorCode.fromException(ex);
            logger.info("Failed to start job: {}", errorMessage);
        }
        
        return execution == null?
            RestResponse.error(errorCode, errorMessage):
            RestResponse.result(createExecutionInfo(execution));
    }
    
    /**
     * Stop the running execution of a job instance.
     * 
     * @param jobName
     * @param jobId The instance id
     * @return
     */
    @PostMapping(value = "/api/jobs/{jobName}/stop/{jobId}")
    public RestResponse<JobExecutionInfo> stop(@PathVariable String jobName, @PathVariable Long jobId)
    {
        JobExecution execution = jobService.findRunningExecution(jobName, jobId);
        if (execution != null) {
            // Stop using execution id
            jobService.stop(execution.getId());
            // Poll for the current execution status
            JobExecution x1 = jobService.findExecution(execution.getId());
            return RestResponse.result(createExecutionInfo(x1));
        }
        
        // Do not regard this as an error (?), simply return null 
        return null;
    }
    
    /**
     * Poll the status of a job instance (list executions for a given instance).
     * 
     * @param jobName
     * @param jobId The instance id
     * @return
     */
    @GetMapping(value = {"/api/jobs/{jobName}/status/{jobId}", "/api/jobs/{jobName}/executions/{jobId}"})
    public RestResponse<List<JobExecutionInfo>> getExecutions(@PathVariable String jobName, @PathVariable Long jobId)
    {
        List<JobExecutionInfo> r = 
            jobService.findExecutions(jobName, jobId).stream()
                .map(x -> createExecutionInfo(x))
                .collect(Collectors.toList());
        
        return RestResponse.result(r);
    }

    /**
     * List all running executions for a given job.
     * 
     * @param jobName
     * @return
     */
    @GetMapping(value = "/api/jobs/{jobName}/running-executions")
    public RestResponse<List<JobExecutionInfo>> getRunningExecutions(@PathVariable String jobName)
    {
        List<JobExecutionInfo> r = 
            jobService.findRunningExecutions(jobName).stream()
                .map(x -> createExecutionInfo(x))
                .collect(Collectors.toList());
        
        return RestResponse.result(r);
    }
    
    /**
     * List instances for a given job.
     * 
     * @param jobName
     * @param start The starting index for this set of results
     * @param count The page size for this set of results
     * @return
     */
    @GetMapping(value = "/api/jobs/{jobName}/instances")
    public RestResponse<List<JobInstanceInfo>> getInstances(
        @PathVariable String jobName, 
        @RequestParam(defaultValue = "0") Integer start, @RequestParam(defaultValue = "25") Integer count)
    {
        final int MAX_PAGE_SIZE = 100;
        
        if (count > MAX_PAGE_SIZE || count <= 0)
            count = MAX_PAGE_SIZE;
        
        logger.info("Fetching instances for job {}: start={} limit={}", 
            jobName, start, count);
        
        List<JobInstanceInfo> r = jobService.findInstances(jobName, start, count).stream()
            .map(y -> createInstanceInfo(y))
            .collect(Collectors.toList());
        
        return RestResponse.result(r);
    }
    
    /**
     * Clear running execution for a given job instance. 
     * 
     * @param jobName
     * @param jobId The instance id
     * @return
     */
    @PostMapping(value = "/api/jobs/{jobName}/clear-running-execution/{jobId}")
    public RestResponse<JobExecutionInfo> clearRunningExecution(@PathVariable String jobName, @PathVariable Long jobId)
    {
        JobExecution x = jobService.clearRunningExecution(jobName, jobId);
        return x == null? 
            null : RestResponse.result(createExecutionInfo(x));
    }
}
