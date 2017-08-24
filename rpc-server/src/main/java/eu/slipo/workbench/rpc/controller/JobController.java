package eu.slipo.workbench.rpc.controller;

import java.io.IOException;
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
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;

import eu.slipo.workbench.rpc.service.JobService;

@RestController
public class JobController
{
    private static final Logger logger = LoggerFactory.getLogger(JobController.class);
    
    private static class ExecutionStatus
    {
        @JsonProperty("status")
        private String statusText = BatchStatus.UNKNOWN.name();
        
        @JsonProperty
        private Long executionId;
        
        @JsonProperty
        private Long instanceId;
        
        @JsonProperty
        private Date startedAt;
        
        @JsonProperty
        private Date finishedAt;
        
        public ExecutionStatus(JobExecution x)
        {
            if (x != null) {
                statusText = x.getStatus().name();
                executionId = x.getId();
                instanceId = x.getJobId();
                startedAt = x.getStartTime();
                finishedAt = x.getEndTime();
            }
        }
    }
    
    @Autowired
    JobService jobService;
    
    /**
     * Submit a new job (JobInstance).
     * 
     * @param jobName
     * @param foo
     */
    @RequestMapping(
        value = "/api/jobs/{jobName}/submit", 
        method = RequestMethod.POST, 
        consumes = "application/json", 
        produces = "application/json")
    public ExecutionStatus submit(@PathVariable String jobName, @RequestParam("foo") String foo)
    {
        // Todo Load default (per job) parameters from database
        // Todo Load request-specific job parameters from request body
        
        // Prepare parameters for a job
        Map<String, JobParameter> parameters = new HashMap<>();
        parameters.put("boo", new JobParameter(199L));
        parameters.put("foo", new JobParameter(foo));
        
        // Launch
        logger.info("Starting job {} with parameters {}", jobName, parameters);
        JobExecution x = jobService.start(jobName, new JobParameters(parameters));
        if (x != null)
            logger.info("Started job as: {}", x);
        
        return new ExecutionStatus(x);
    }
    
    /**
     * Stop the running execution of job instance.
     * 
     * @param jobName
     * @param jobId
     * @return
     */
    @RequestMapping(
        value = "/api/jobs/{jobName}/stop/{executionId}", 
        method = RequestMethod.POST, 
        produces = "application/json")
    public ExecutionStatus stop(@PathVariable String jobName, @PathVariable Long executionId)
    {
        JobExecution x = jobService.findExecution(executionId);
        if (x != null) {
            jobService.stop(x.getId());
            return new ExecutionStatus(jobService.findExecution(x.getId()));
        }
        return null;
    }
    
    /**
     * Poll the status of a job (JobInstance)
     * 
     * @param jobName
     * @param jobId
     * @return
     */
    @RequestMapping(
        value = "/api/jobs/{jobName}/status/{jobId}", 
        method = RequestMethod.GET,
        produces = "application/json")
    public List<ExecutionStatus> poll(@PathVariable String jobName, @PathVariable Long jobId)
    {
        
        List<JobExecution> executions = jobService.findExecutions(jobName, jobId);
        
        for (JobExecution x: executions) {
            logger.info("Polled status {}#{}: {}", jobName, jobId, x);
        }
        
        return executions.stream()
            .map(x -> new ExecutionStatus(x))
            .collect(Collectors.toList());
    }

    /**
     * Get running executions for a given job.
     * 
     * @param jobName
     * @return
     */
    @RequestMapping(
        value = "/api/jobs/{jobName}/running-executions", 
        method = RequestMethod.GET,
        produces = "application/json")
    public List<ExecutionStatus> findRunningExecutions(@PathVariable String jobName)
    {
        Set<JobExecution> jx = jobService.findRunningExecutions(jobName);
        return jx.stream().map(x -> new ExecutionStatus(x))
            .collect(Collectors.toList());
    }
    
    /**
     * Clear running execution for a given job instance. 
     * 
     * @param jobName
     * @param jobId
     * @return
     */
    @RequestMapping(
        value = "/api/jobs/{jobName}/clear-running-execution/{jobId}",
            method = RequestMethod.POST,
        produces = "application/json")
    public ExecutionStatus clearRunningExecution(@PathVariable String jobName, @PathVariable Long jobId)
    {
        JobExecution x = jobService.clearRunningExecution(jobName, jobId);
        return x == null? null : new ExecutionStatus(x);
    }
}
