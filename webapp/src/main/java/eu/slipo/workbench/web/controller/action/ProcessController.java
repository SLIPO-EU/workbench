package eu.slipo.workbench.web.controller.action;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.workbench.common.model.BasicErrorCode;
import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.common.model.QueryResultPage;
import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.model.process.EnumProcessTaskType;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessErrorCode;
import eu.slipo.workbench.common.model.process.ProcessExecutionNotFoundException;
import eu.slipo.workbench.common.model.process.ProcessExecutionQuery;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessNotFoundException;
import eu.slipo.workbench.common.model.process.ProcessQuery;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.web.model.QueryResult;
import eu.slipo.workbench.web.model.process.EnumProcessSaveActionType;
import eu.slipo.workbench.web.model.process.ProcessCreateRequest;
import eu.slipo.workbench.web.model.process.ProcessExecutionQueryRequest;
import eu.slipo.workbench.web.model.process.ProcessExecutionRecordView;
import eu.slipo.workbench.web.model.process.ProcessQueryRequest;
import eu.slipo.workbench.web.model.process.ProcessRecordView;
import eu.slipo.workbench.web.service.ProcessService;

/**
 * Actions for managing processes
 */
@RestController
@Secured({ "ROLE_USER", "ROLE_AUTHOR", "ROLE_ADMIN" })
@RequestMapping(produces = "application/json")
public class ProcessController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ProcessController.class);

    @Autowired
    private ProcessService processService;

    /**
     * Search for processes
     *
     * @param data the query to execute
     * @return a list of processes
     */
    @RequestMapping(value = "/action/process/query", method = RequestMethod.POST)
    public RestResponse<QueryResult<ProcessRecord>> find(@RequestBody ProcessQueryRequest request) {

        if (request == null || request.getQuery() == null) {
            return RestResponse.error(ProcessErrorCode.QUERY_IS_EMPTY, "The query is empty");
        }

        ProcessQuery query = request.getQuery();
        PageRequest pageRequest = request.getPageRequest();
        QueryResultPage<ProcessRecord> r = this.processService.find(query, pageRequest);

        return RestResponse.result(QueryResult.create(r));
    }

    /**
     * Search for process templates
     *
     * @param data the query to execute
     * @return a list of process templates
     */
    @RequestMapping(value = "/action/process/template/query", method = RequestMethod.POST)
    public RestResponse<QueryResult<ProcessRecord>> findTemplates(@RequestBody ProcessQueryRequest request) {

        if (request == null || request.getQuery() == null) {
            return RestResponse.error(ProcessErrorCode.QUERY_IS_EMPTY, "The query is empty");
        }

        ProcessQuery query = request.getQuery();
        PageRequest pageRequest = request.getPageRequest();
        QueryResultPage<ProcessRecord> r = this.processService.findTemplates(query, pageRequest);

        return RestResponse.result(QueryResult.create(r));
    }

    /**
     * Search for process executions
     *
     * @param data the query to execute
     * @return a list of processes
     */
    @RequestMapping(value = "/action/process/execution/query", method = RequestMethod.POST)
    public RestResponse<QueryResult<ProcessExecutionRecord>> find(@RequestBody ProcessExecutionQueryRequest request) {

        if (request == null || request.getQuery() == null) {
            return RestResponse.error(ProcessErrorCode.QUERY_IS_EMPTY, "The query is empty");
        }

        ProcessExecutionQuery query = request.getQuery();
        PageRequest pageRequest = request.getPageRequest();
        QueryResultPage<ProcessExecutionRecord> r = this.processService.find(query, pageRequest);

        return RestResponse.result(QueryResult.create(r));
    }

    /**
     * Get all executions for a process with a specific id and version. Execution steps
     * are not included
     *
     * @param id the process id
     * @param version the process version
     * @return a list of {@link ProcessExecutionRecord}
     */
    @RequestMapping(value = "/action/process/{id}/{version}/execution", method = RequestMethod.GET)
    public RestResponse<List<ProcessExecutionRecord>> getAllProcessExecutions(@PathVariable long id, @PathVariable long version) {

        return RestResponse.result(processService.findExecutions(id, version));
    }

    /**
     * Get an execution for a process with a specific id and version. The response
     * includes the execution steps
     *
     * @param id the process id
     * @param version the process version
     * @param executionId the execution id
     * @return a list of {@link ProcessExecutionRecord}
     */
    @RequestMapping(value = "/action/process/{id}/{version}/execution/{executionId}", method = RequestMethod.GET)
    public RestResponse<?> getProcessExecution(@PathVariable long id, @PathVariable long version, @PathVariable long executionId) {

        try {
            final ProcessExecutionRecordView view = this.processService.getProcessExecution(id, version, executionId);
            return RestResponse.result(view);
        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }
    }

    /**
     * Get KPI data for the selected execution
     *
     * @param id the process id
     * @param version the process version
     * @param executionId the execution id
     * @param fileId the file id
     * @return a JSON object with KPI data
     */
    @RequestMapping(value = "/action/process/{id}/{version}/execution/{executionId}/kpi/{fileId}", method = RequestMethod.GET)
    public RestResponse<?> getProcessExecutionKpiData(
        @PathVariable long id, @PathVariable long version, @PathVariable long executionId, @PathVariable long fileId) {

        try {
            Object data = this.processService.getProcessExecutionKpiData(id, version, executionId, fileId);
            return RestResponse.result(data);
        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }
    }

    /**
     * Checks if a file for the selected execution exists
     *
     * @param id the process id
     * @param version the process version
     * @param executionId the execution id
     * @param fileId the file id
     * @return a list of {@link ProcessExecutionRecord}
     * @throws IOException if process or file is not found
     */
    @RequestMapping(value = "/action/process/{id}/{version}/execution/{executionId}/file/{fileId}/exists", method = RequestMethod.GET)
    public RestResponse<?> processExecutionFileExists(
        @PathVariable long id, @PathVariable long version, @PathVariable long executionId, @PathVariable long fileId,
        HttpServletResponse response
    ) throws IOException {

        final File file;
        try {
            file = this.processService.getProcessExecutionFile(id, version, executionId, fileId);
            if ((file != null) && (file.exists())) {
                return RestResponse.success();
            }
        } catch (ProcessNotFoundException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Process was not found");
        } catch (ProcessExecutionNotFoundException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Process execution was not found");
        }

        response.sendError(HttpServletResponse.SC_GONE, "File has been removed");
        return null;
    }

    /**
     * Downloads a file for the selected execution
     *
     * @param id the process id
     * @param version the process version
     * @param executionId the execution id
     * @param fileId the file id
     * @return an instance of {@link FileSystemResource}
     * @throws IOException if an I/O exception has occurred
     */
    @RequestMapping(value = "/action/process/{id}/{version}/execution/{executionId}/file/{fileId}/download", method = RequestMethod.GET)
    public FileSystemResource downloadProcessExecutionFile(
        @PathVariable long id, @PathVariable long version, @PathVariable long executionId, @PathVariable long fileId,
        HttpServletResponse response
    ) throws IOException {

        final File file;
        try {
            file = this.processService.getProcessExecutionFile(id, version, executionId, fileId);
            if ((file != null) && (file.exists())) {
                response.setHeader("Content-Disposition", String.format("attachment; filename=%s", file.getName()));
                return new FileSystemResource(file);
            }
        } catch (ProcessNotFoundException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Process was not found");
        } catch (ProcessExecutionNotFoundException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Process execution was not found");
        }

        response.sendError(HttpServletResponse.SC_GONE, "File has been removed");
        return null;
    }

    /**
     * Loads the most recent version of an existing process
     *
     * @param id the process id
     * @return the updated process model
     */
    @RequestMapping(value = "/action/process/{id}", method = RequestMethod.GET)
    public RestResponse<?> getProcess(@PathVariable long id) {
        try {
            ProcessRecord record = processService.findOne(id);
            if (record == null) {
                return RestResponse.error(new Error(ProcessErrorCode.PROCESS_NOT_FOUND, "Process was not found"));
            }
            if (record.getDefinition() == null) {
                return RestResponse.error(new Error(ProcessErrorCode.INVALID, "Failed to parse process definition"));
            }
            return RestResponse.result(new ProcessRecordView(record));
        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }
    }

    /**
     * Loads the specific version of an existing process
     *
     * @param data the process model
     * @return the updated process model
     */
    @RequestMapping(value = "/action/process/{id}/{version}", method = RequestMethod.GET)
    public RestResponse<?> getProcessRevision(@PathVariable long id, @PathVariable long version) {
        try {
            ProcessRecord record = processService.findOne(id, version);
            if (record == null) {
                return RestResponse.error(new Error(ProcessErrorCode.PROCESS_NOT_FOUND, "Process was not found"));
            }
            if (record.getDefinition() == null) {
                return RestResponse.error(new Error(ProcessErrorCode.INVALID, "Failed to parse process definition"));
            }
            return RestResponse.result(new ProcessRecordView(record));
        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }
    }

    /**
     * Create a new process by providing a process definition
     *
     * @param request the process definition
     *
     * @return the created process model
     */
    @RequestMapping(value = "/action/process", method = RequestMethod.POST)
    public RestResponse<?> create(@RequestBody ProcessCreateRequest request) {

        return this.createOrUpdateProcess(null, request);
    }

    /**
     * Update an existing process by providing a newer process definition
     *
     * @param id The ID of the process to be updated
     * @param request the process definition
     * @return the created process model
     */
    @RequestMapping(value = "/action/process/{id}", method = RequestMethod.POST)
    public RestResponse<?> update(@PathVariable long id, @RequestBody ProcessCreateRequest request) {

        return this.createOrUpdateProcess(id, request);
    }

    /**
     * Starts the current version of the selected process.
     *
     * @param id the id of the process to start
     * @return an empty response if operation was successful
     */
    @RequestMapping(value = "/action/process/{id}/{version}/start", method = RequestMethod.POST)
    public RestResponse<?> start(@PathVariable long id, @PathVariable long version) {
        try {
            this.processService.start(id, version, EnumProcessTaskType.UNDEFINED);
        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }
        return RestResponse.success();
    }

    /**
     * Starts the current version of the selected process.
     *
     * @param id the id of the process to start
     * @return an empty response if operation was successful
     */
    @RequestMapping(value = "/action/process/{id}/{version}/stop", method = RequestMethod.POST)
    public RestResponse<?> stop(@PathVariable long id, @PathVariable long version) {
        try {
            this.processService.stop(id, version);
        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }
        return RestResponse.success();
    }

    /**
     * Exports process execution data to relational database for rendering maps.
     *
     * @param id the id of the process
     * @param version the process version
     * @param execution the unique id of the process execution instance to export
     * @return an empty response if operation was successful
     */
    @RequestMapping(value = "/action/process/{id}/{version}/export/{execution}", method = RequestMethod.POST)
    public RestResponse<?> exportMap(@PathVariable long id, @PathVariable long version, @PathVariable long execution) {
        try {
            this.processService.exportMap(id, version, execution);
        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }
        return RestResponse.success();
    }

    /**
     * Create/Update a new/existing process definition
     *
     * @param id process id for updating an existing process
     * @param request the process definition
     * @return the created or updated process model
     */
    private RestResponse<?> createOrUpdateProcess(Long id, ProcessCreateRequest request) {
        ProcessRecord process = null;
        ProcessExecutionRecord execution = null;

        // Save
        try {
            final ProcessDefinition definition = request.getDefinition();
            if (definition == null) {
                return RestResponse.error(BasicErrorCode.INPUT_INVALID, "No process definition");
            }
            final ProcessDefinition normalizedDefinition = ProcessDefinition.normalize(definition);

            final boolean isTemplate = request.getAction() == EnumProcessSaveActionType.SAVE_TEMPLATE;

            if (id == null) {
                process = processService.create(normalizedDefinition, isTemplate);
            } else {
                process = processService.update(id, normalizedDefinition, isTemplate);
            }
        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }

        // Execute
        try {
            if ((request.getAction() == EnumProcessSaveActionType.SAVE_AND_EXECUTE) ||
                (request.getAction() == EnumProcessSaveActionType.SAVE_AND_EXECUTE_AND_MAP)){
                execution = this.processService.start(process.getId(), process.getVersion(), EnumProcessTaskType.DATA_INTEGRATION);
            }
        } catch (Exception ex) {
            return this.exceptionToResponse(ex, Error.EnumLevel.WARN);
        }

        // Schedule map creation
        try {
            if (request.getAction() == EnumProcessSaveActionType.SAVE_AND_EXECUTE_AND_MAP) {
                this.processService.exportMap(execution);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return RestResponse.error(BasicErrorCode.UNKNOWN, "Failed to scheduler map creation.", Error.EnumLevel.WARN);
        }

        return RestResponse.result(new ProcessRecordView(process));
    }

    private RestResponse<?> exceptionToResponse(Exception ex) {
        logger.error(ex.getMessage(), ex);
        return exceptionToResponse(ex, Error.EnumLevel.ERROR);
    }

}
