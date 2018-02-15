package eu.slipo.workbench.rpc.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;

@RestController
@RequestMapping(produces = "application/json")
public class ProcessController
{
    private static final Logger logger = LoggerFactory.getLogger(ProcessController.class);
    
    public static class Request {}
     
    @PostMapping(value = "/api/process/start/{id}")
    public RestResponse<ProcessExecutionRecord> start(@RequestParam("id") Integer id)
    {
        // Todo Build a workflow from this process descriptor (using latest version).
        // A workflow instance should be 1-1 mapped to a process revision entity.
        // As a consequence, a process revision can only have a single active execution at 
        // a given time (because a workflow does so).  
        
        // Todo Create a process-execution entity and associate with captured events
        
        // Todo return a DTO for process execution (ProcessExecutionRecord)
                
        return null;
    }
    
    @PostMapping(value = "/api/process/stop/{executionId}")
    public RestResponse<?> stop(@RequestParam("executionId") Integer executionId)
    {
        // Todo Find and stop workflow associated with process of given execution-id
        
        return null;
    }
    
    @GetMapping(value = "/api/process/status/{executionId}")
    public RestResponse<?> status(@RequestParam("executionId") Integer executionId) 
        throws Exception
    {
        // Todo  Find and query workflow associated with process
        
        return null;
    }
}
