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
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;

import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.model.BasicErrorCode;
import eu.slipo.workbench.common.model.ErrorCode;
import eu.slipo.workbench.rpc.model.JobExecutionErrorCode;
import eu.slipo.workbench.rpc.service.JobService;

@RestController
@RequestMapping(produces = "application/json")
public class JobController
{
    private static final Logger logger = LoggerFactory.getLogger(JobController.class);
    
    private static class ExecutionStatus
    {
        @JsonProperty("status")
        private String statusText = BatchStatus.UNKNOWN.name();
        
        /**
         * The job execution id.
         */
        @JsonProperty
        private Long xid;
        
        /**
         * The job instance id.
         */
        @JsonProperty
        private Long id;
        
        @JsonProperty
        private Date started;
        
        @JsonProperty
        private Date finished;
        
        public ExecutionStatus(JobExecution x)
        {
            if (x != null) {
                statusText = x.getStatus().name();
                xid = x.getId();
                id = x.getJobId();
                started = x.getStartTime();
                finished = x.getEndTime();
            }
        }
    }
    
    @Autowired
    JobService jobService;
    
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
    public RestResponse<ExecutionStatus> submit(@PathVariable String jobName, @RequestParam("foo") String foo)
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
            RestResponse.result(new ExecutionStatus(execution));
    }
    
    /**
     * Stop the running execution of a job instance.
     * 
     * @param jobName
     * @param jobId The instance id
     * @return
     */
    @PostMapping(value = "/api/jobs/{jobName}/stop/{jobId}")
    public RestResponse<ExecutionStatus> stop(@PathVariable String jobName, @PathVariable Long jobId)
    {
        JobExecution execution = jobService.findRunningExecution(jobName, jobId);
        if (execution != null) {
            // Stop using execution id
            jobService.stop(execution.getId());
            // Poll for the current execution status
            JobExecution x1 = jobService.findExecution(execution.getId());
            return RestResponse.result(new ExecutionStatus(x1));
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
    public RestResponse<List<ExecutionStatus>> getExecutions(@PathVariable String jobName, @PathVariable Long jobId)
    {
        List<ExecutionStatus> r = 
            jobService.findExecutions(jobName, jobId).stream()
                .map(x -> new ExecutionStatus(x))
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
    public RestResponse<List<ExecutionStatus>> getRunningExecutions(@PathVariable String jobName)
    {
        List<ExecutionStatus> r = 
            jobService.findRunningExecutions(jobName).stream()
                .map(x -> new ExecutionStatus(x))
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
    public RestResponse<ExecutionStatus> clearRunningExecution(@PathVariable String jobName, @PathVariable Long jobId)
    {
        JobExecution x = jobService.clearRunningExecution(jobName, jobId);
        return x == null? 
            null : RestResponse.result(new ExecutionStatus(x));
    }
}
