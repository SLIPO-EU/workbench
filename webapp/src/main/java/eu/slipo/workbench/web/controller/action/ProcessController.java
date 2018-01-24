package eu.slipo.workbench.web.controller.action;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.web.model.QueryResult;
import eu.slipo.workbench.web.model.process.EnumProcessTask;
import eu.slipo.workbench.web.model.process.ProcessCreateRequest;
import eu.slipo.workbench.web.model.process.ProcessDefinitionView;
import eu.slipo.workbench.web.model.process.ProcessErrorCode;
import eu.slipo.workbench.web.model.process.ProcessExecutionQuery;
import eu.slipo.workbench.web.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.web.model.process.ProcessQuery;
import eu.slipo.workbench.web.model.process.ProcessRecord;
import eu.slipo.workbench.web.model.process.ProcessViewResult;
import eu.slipo.workbench.web.repository.IProcessRepository;
import eu.slipo.workbench.web.service.IProcessService;

/**
 * Actions for managing processes
 */
@RestController
public class ProcessController {

    @Autowired
    private IProcessRepository processRepository;

    @Autowired
    private IProcessService processService;

    /**
     * Search for processes
     *
     * @param authentication the authenticated principal
     * @param data the query to execute
     * @return a list of processes
     */
    @RequestMapping(value = "/action/process/query", method = RequestMethod.POST, produces = "application/json")
    public RestResponse<QueryResult<ProcessRecord>> find(Authentication authentication, @RequestBody ProcessQuery query) {

        if (query == null) {
            RestResponse.error(ProcessErrorCode.QUERY_IS_EMPTY, "The query is empty");
        }

        query.setTask(EnumProcessTask.DATA_INTEGRATION);
        query.setTemplate(false);

        QueryResult<ProcessRecord> result = this.processRepository.find(query);

        return RestResponse.result(result);
    }

    /**
     * Search for process executions
     *
     * @param authentication the authenticated principal
     * @param data the query to execute
     * @return a list of processes
     */
    @RequestMapping(value = "/action/process/execution/query", method = RequestMethod.POST, produces = "application/json")
    public RestResponse<QueryResult<ProcessExecutionRecord>> find(Authentication authentication, @RequestBody ProcessExecutionQuery query) {

        if (query == null) {
            RestResponse.error(ProcessErrorCode.QUERY_IS_EMPTY, "The query is empty");
        }

        QueryResult<ProcessExecutionRecord> result = this.processRepository.find(query);

        return RestResponse.result(result);
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
    @RequestMapping(value = "/action/process/{id}/{version}/execution", method = RequestMethod.GET, produces = "application/json")
    public RestResponse<List<ProcessExecutionRecord>> getAllProcessExecutions(Authentication authentication, @PathVariable long id, @PathVariable long version) {

        return RestResponse.<List<ProcessExecutionRecord>>result(this.processService.findExecutions(id, version));
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
    @RequestMapping(value = "/action/process/{processId}/{processVersion}/execution/{executionId}", method = RequestMethod.GET, produces = "application/json")
    public RestResponse<ProcessExecutionRecord> getProcessExecution(
            Authentication authentication,
            @PathVariable long processId,
            @PathVariable long processVersion,
            @PathVariable long executionId) {

        ProcessExecutionRecord record = processRepository.findOne(processId, processVersion, executionId);
        if (record == null) {
            return RestResponse.<ProcessExecutionRecord>error(new Error(ProcessErrorCode.NOT_FOUND, "Execution was not found"));
        }
        return RestResponse.<ProcessExecutionRecord>result(record);
    }

    /**
     * Loads the most recent version of an existing process
     *
     * @param authentication the authenticated principal
     * @param data the process model
     * @return the updated process model
     */
    @RequestMapping(value = "/action/process/{id}", method = RequestMethod.GET, produces = "application/json")
    public RestResponse<ProcessViewResult> findOne(Authentication authentication, @PathVariable long id) {

        ProcessDefinitionView process = this.processService.findOne(id);
        if(process == null) {
            return RestResponse.<ProcessViewResult>error(new Error(ProcessErrorCode.NOT_FOUND, "Process was not found"));
        }
        return RestResponse.<ProcessViewResult>result(new ProcessViewResult(process));
    }

    /**
     * Loads the specific version of an existing process
     *
     * @param authentication the authenticated principal
     * @param data the process model
     * @return the updated process model
     */
    @RequestMapping(value = "/action/process/{id}/{version}", method = RequestMethod.GET, produces = "application/json")
    public RestResponse<ProcessViewResult> findOne(Authentication authentication, @PathVariable long id, @PathVariable long version) {

        ProcessDefinitionView process = this.processService.findOne(id, version);
        if(process == null) {
            return RestResponse.<ProcessViewResult>error(new Error(ProcessErrorCode.NOT_FOUND, "Process was not found"));
        }
        return RestResponse.<ProcessViewResult>result(new ProcessViewResult(process));
    }

    /**
     * Creates/Updates a new/existing process
     *
     * @param authentication the authenticated principal
     * @param data the process model
     * @return the updated process model
     */
    @RequestMapping(value = "/action/process", method = RequestMethod.POST, produces = "application/json")
    public RestResponse<ProcessViewResult> update(Authentication authentication, @RequestBody ProcessCreateRequest request) {

        List<Error> errors = this.processService.update(request.getProcess());

        return RestResponse.<ProcessViewResult>error(errors);
    }

}
