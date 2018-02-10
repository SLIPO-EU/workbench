package eu.slipo.workbench.web.controller.action;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.common.model.QueryResultPage;
import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.model.process.EnumProcessTaskType;
import eu.slipo.workbench.common.model.process.ProcessDefinitionView;
import eu.slipo.workbench.common.model.process.ProcessErrorCode;
import eu.slipo.workbench.common.model.process.ProcessExecutionQuery;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessQuery;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.common.model.process.ProcessViewResult;
import eu.slipo.workbench.common.repository.ProcessRepository;
import eu.slipo.workbench.web.model.QueryResult;
import eu.slipo.workbench.web.model.process.ProcessCreateRequest;
import eu.slipo.workbench.web.model.process.ProcessExecutionQueryRequest;
import eu.slipo.workbench.web.model.process.ProcessQueryRequest;
import eu.slipo.workbench.web.service.AuthenticationFacade;
import eu.slipo.workbench.web.service.ProcessService;

/**
 * Actions for managing processes
 */
@RestController
@RequestMapping(produces = "application/json")
public class ProcessController {

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private ProcessService processService;

    @Autowired
    private AuthenticationFacade authenticationFacade;
    
    /**
     * Search for processes
     *
     * @param authentication the authenticated principal
     * @param data the query to execute
     * @return a list of processes
     */
    @RequestMapping(value = "/action/process/query", method = RequestMethod.POST)
    public RestResponse<QueryResult<ProcessRecord>> find(
        Authentication authentication, @RequestBody ProcessQueryRequest request) 
    {
        if (request == null || request.getQuery() == null) {
            return RestResponse.error(ProcessErrorCode.QUERY_IS_EMPTY, "The query is empty");
        }

        ProcessQuery query = request.getQuery();
        query.setTaskType(EnumProcessTaskType.DATA_INTEGRATION);
        query.setTemplate(false);
        query.setCreatedBy(authenticationFacade.getCurrentUserId());
        
        QueryResultPage<ProcessRecord> r = processRepository.find(query, request.getPageRequest());

        return RestResponse.result(QueryResult.create(r));
    }

    /**
     * Search for process executions
     *
     * @param authentication the authenticated principal
     * @param data the query to execute
     * @return a list of processes
     */
    @RequestMapping(value = "/action/process/execution/query", method = RequestMethod.POST)
    public RestResponse<QueryResult<ProcessExecutionRecord>> find(
        Authentication authentication, @RequestBody ProcessExecutionQueryRequest request) 
    {
        if (request == null || request.getQuery() == null) {
            return RestResponse.error(ProcessErrorCode.QUERY_IS_EMPTY, "The query is empty");
        }
        
        ProcessExecutionQuery query = request.getQuery();
        query.setCreatedBy(authenticationFacade.getCurrentUserId());
        
        PageRequest pageReq = request.getPageRequest();
        
        QueryResultPage<ProcessExecutionRecord> r = processRepository.find(query, pageReq);

        return RestResponse.result(QueryResult.create(r));
    }

    /**
     * Get all executions for a process with a specific id and version. Execution steps
     * are not included
     *
     * @param authentication the authenticated principal
     * @param id the process id
     * @param version the process version
     * @return a list of {@link ProcessExecutionRecord}
     */
    @RequestMapping(value = "/action/process/{id}/{version}/execution", method = RequestMethod.GET)
    public RestResponse<List<ProcessExecutionRecord>> getAllProcessExecutions(
        Authentication authentication, @PathVariable long id, @PathVariable long version) 
    {
        return RestResponse.result(processService.findExecutions(id, version));
    }

    /**
     * Get an execution for a process with a specific id and version. The response
     * includes the execution steps
     *
     * @param authentication the authenticated principal
     * @param processId the process id
     * @param processVersion the process version
     * @param executionId the execution id
     * @return a list of {@link ProcessExecutionRecord}
     */
    @RequestMapping(
        value = "/action/process/{processId}/{processVersion}/execution/{executionId}", method = RequestMethod.GET)
    public RestResponse<ProcessExecutionRecord> getProcessExecution(
        Authentication authentication, 
        @PathVariable long processId, @PathVariable long processVersion, @PathVariable long executionId) 
    {
        ProcessExecutionRecord record = processRepository.findOne(processId, processVersion, executionId);
        if (record == null) {
            return RestResponse.error(new Error(ProcessErrorCode.NOT_FOUND, "Execution was not found"));
        }
        return RestResponse.result(record);
    }

    /**
     * Loads the most recent version of an existing process
     *
     * @param authentication the authenticated principal
     * @param data the process model
     * @return the updated process model
     */
    @RequestMapping(value = "/action/process/{id}", method = RequestMethod.GET)
    public RestResponse<ProcessViewResult> findOne(Authentication authentication, @PathVariable long id) {

        ProcessDefinitionView process = this.processService.findOne(id);
        if(process == null) {
            return RestResponse.error(new Error(ProcessErrorCode.NOT_FOUND, "Process was not found"));
        }
        return RestResponse.result(new ProcessViewResult(process));
    }

    /**
     * Loads the specific version of an existing process
     *
     * @param authentication the authenticated principal
     * @param data the process model
     * @return the updated process model
     */
    @RequestMapping(value = "/action/process/{id}/{version}", method = RequestMethod.GET)
    public RestResponse<ProcessViewResult> findOne(
        Authentication authentication, @PathVariable long id, @PathVariable long version) 
    {
        ProcessDefinitionView process = this.processService.findOne(id, version);
        if(process == null) {
            return RestResponse.error(new Error(ProcessErrorCode.NOT_FOUND, "Process was not found"));
        }
        return RestResponse.result(new ProcessViewResult(process));
    }

    /**
     * Creates/Updates a new/existing process
     *
     * @param authentication the authenticated principal
     * @param data the process model
     * @return the updated process model
     */
    @RequestMapping(value = "/action/process", method = RequestMethod.POST)
    public RestResponse<ProcessViewResult> update(
        Authentication authentication, @RequestBody ProcessCreateRequest request) 
    {
        List<Error> errors = this.processService.update(request.getProcess());
        return RestResponse.error(errors);
    }
}
