package eu.slipo.workbench.web.controller.action;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.workbench.common.model.ApplicationException;
import eu.slipo.workbench.common.model.BasicErrorCode;
import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.common.model.QueryResultPage;
import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.model.process.CatalogResource;
import eu.slipo.workbench.common.model.process.EnumInputType;
import eu.slipo.workbench.common.model.process.EnumProcessTaskType;
import eu.slipo.workbench.common.model.process.InvalidProcessDefinitionException;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessErrorCode;
import eu.slipo.workbench.common.model.process.ProcessExecutionQuery;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStartException;
import eu.slipo.workbench.common.model.process.ProcessNotFoundException;
import eu.slipo.workbench.common.model.process.ProcessQuery;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.common.model.resource.ResourceRecord;
import eu.slipo.workbench.common.repository.ProcessRepository;
import eu.slipo.workbench.common.repository.ResourceRepository;
import eu.slipo.workbench.web.model.QueryResult;
import eu.slipo.workbench.web.model.process.EnumProcessSaveActionType;
import eu.slipo.workbench.web.model.process.ProcessCreateRequest;
import eu.slipo.workbench.web.model.process.ProcessExecutionQueryRequest;
import eu.slipo.workbench.web.model.process.ProcessExecutionRecordView;
import eu.slipo.workbench.web.model.process.ProcessQueryRequest;
import eu.slipo.workbench.web.model.process.ProcessRecordView;
import eu.slipo.workbench.web.service.IAuthenticationFacade;
import eu.slipo.workbench.web.service.ProcessService;

/**
 * Actions for managing processes
 */
@RestController
@Secured({ "ROLE_USER", "ROLE_ADMIN" })
@RequestMapping(produces = "application/json")
public class ProcessController {

    private static final Logger logger = LoggerFactory.getLogger(ProcessController.class);

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private ProcessService processService;

    @Autowired
    private IAuthenticationFacade authenticationFacade;

    /**
     * Search for processes
     *
     * @param authentication the authenticated principal
     * @param data the query to execute
     * @return a list of processes
     */
    @RequestMapping(value = "/action/process/query", method = RequestMethod.POST)
    public RestResponse<QueryResult<ProcessRecord>> find(@RequestBody ProcessQueryRequest request) {

        if (request == null || request.getQuery() == null) {
            return RestResponse.error(ProcessErrorCode.QUERY_IS_EMPTY, "The query is empty");
        }

        ProcessQuery query = request.getQuery();
        query.setTaskType(EnumProcessTaskType.DATA_INTEGRATION);
        query.setTemplate(false);
        query.setCreatedBy(authenticationFacade.getCurrentUserId());

        PageRequest pageRequest = request.getPageRequest();
        QueryResultPage<ProcessRecord> r = processRepository.query(query, pageRequest);

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
    public RestResponse<QueryResult<ProcessExecutionRecord>> find(@RequestBody ProcessExecutionQueryRequest request) {

        if (request == null || request.getQuery() == null) {
            return RestResponse.error(ProcessErrorCode.QUERY_IS_EMPTY, "The query is empty");
        }

        ProcessExecutionQuery query = request.getQuery();
        query.setCreatedBy(authenticationFacade.getCurrentUserId());

        PageRequest pageRequest = request.getPageRequest();
        QueryResultPage<ProcessExecutionRecord> r = processRepository.queryExecutions(query, pageRequest);

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
    public RestResponse<List<ProcessExecutionRecord>> getAllProcessExecutions(@PathVariable long id, @PathVariable long version) {

        return RestResponse.result(processService.findExecutions(id, version));
    }

    /**
     * Get an execution for a process with a specific id and version. The response
     * includes the execution steps
     *
     * @param authentication the authenticated principal
     * @param id the process id
     * @param version the process version
     * @param executionId the execution id
     * @return a list of {@link ProcessExecutionRecord}
     */
    @RequestMapping(value = "/action/process/{id}/{version}/execution/{executionId}", method = RequestMethod.GET)
    public RestResponse<ProcessExecutionRecordView> getProcessExecution(
        @PathVariable long id, @PathVariable long version, @PathVariable long executionId) {

        ProcessRecord processRecord = processRepository.findOne(id, version, false);
        ProcessExecutionRecord executionRecord = processRepository.findExecution(executionId);
        if (processRecord == null ||
            executionRecord == null ||
            executionRecord.getProcess().getId() != id ||
            executionRecord.getProcess().getVersion() != version) {
            return RestResponse.error(new Error(ProcessErrorCode.NOT_FOUND, "Execution was not found"));
        }

        // For catalog resources update bounding box and table name values
        processRecord
            .getDefinition()
            .resources()
            .stream()
            .filter(r->r.getInputType() == EnumInputType.CATALOG)
            .map(r-> (CatalogResource) r)
            .forEach(r1-> {
                ResourceRecord r2 = resourceRepository.findOne(r1.getId(), r1.getVersion());
                if (r2 != null) {
                    r1.setBoundingBox(r2.getBoundingBox());
                    r1.setTableName(r2.getTableName());
                }
            });

        return RestResponse.result(new ProcessExecutionRecordView(processRecord, executionRecord));
    }

    /**
     * Loads the most recent version of an existing process
     *
     * @param authentication the authenticated principal
     * @param id the process id
     * @return the updated process model
     */
    @RequestMapping(value = "/action/process/{id}", method = RequestMethod.GET)
    public RestResponse<ProcessRecordView> getProcess(@PathVariable long id) {

        ProcessRecord record = processService.findOne(id);
        if (record == null) {
            return RestResponse.error(new Error(ProcessErrorCode.NOT_FOUND, "Process was not found"));
        }
        return RestResponse.result(new ProcessRecordView(record));
    }

    /**
     * Loads the specific version of an existing process
     *
     * @param authentication the authenticated principal
     * @param data the process model
     * @return the updated process model
     */
    @RequestMapping(value = "/action/process/{id}/{version}", method = RequestMethod.GET)
    public RestResponse<ProcessRecordView> getProcessRevision(@PathVariable long id, @PathVariable long version) {

        ProcessRecord record = processService.findOne(id, version);
        if (record == null) {
            return RestResponse.error(new Error(ProcessErrorCode.NOT_FOUND, "Process was not found"));
        }
        return RestResponse.result(new ProcessRecordView(record));
    }

    /**
     * Create a new process by providing a process definition
     *
     * @param authentication the authenticated principal
     * @param request
     *
     * @return the created process model
     */
    @RequestMapping(value = "/action/process", method = RequestMethod.POST)
    public RestResponse<ProcessRecordView> create(@RequestBody ProcessCreateRequest request) {

        try {
            final ProcessDefinition definition = request.getDefinition();
            if (definition == null) {
                return RestResponse.error(BasicErrorCode.INPUT_INVALID, "No process definition");
            }

            final ProcessRecord record = processService.create(definition);
            if (request.getAction() == EnumProcessSaveActionType.SAVE_AND_EXECUTE) {
                this.processService.start(record.getId(), record.getVersion());
            }

            return RestResponse.result(new ProcessRecordView(record));

            // TODO : Add single error handler
        } catch (ProcessNotFoundException e) {
            return RestResponse.error(ProcessErrorCode.NOT_FOUND, "Process was not found");
        } catch (ProcessExecutionStartException e) {
            return RestResponse.error(ProcessErrorCode.FAILED_TO_START, "Process execution has failed to start");
        } catch (IOException e) {
            return RestResponse.error(BasicErrorCode.IO_ERROR, "An unknown error has occurred");
        } catch (InvalidProcessDefinitionException ex) {
            return RestResponse.error(ex.getErrors());
        } catch (ApplicationException ex) {
            return RestResponse.error(ex.toError());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return RestResponse.error(BasicErrorCode.UNKNOWN, "An unknown error has occurred");
        }
    }

    /**
     * Update an existing process by providing a newer process definition
     *
     * @param authentication the authenticated principal
     * @param id The ID of the process to be updated
     * @param request
     * @return the created process model
     */
    @RequestMapping(value = "/action/process/{id}", method = RequestMethod.POST)
    public RestResponse<ProcessRecordView> update(@PathVariable long id, @RequestBody ProcessCreateRequest request) {

        try {
            final ProcessDefinition definition = request.getDefinition();
            if (definition == null) {
                return RestResponse.error(BasicErrorCode.INPUT_INVALID, "No process definition");
            }

            final ProcessRecord record = processService.update(id, definition);
            if (request.getAction() == EnumProcessSaveActionType.SAVE_AND_EXECUTE) {
                this.processService.start(record.getId(), record.getVersion());
            }

            return RestResponse.result(new ProcessRecordView(record));

            // TODO : Add single error handler
        } catch (ProcessNotFoundException e) {
            return RestResponse.error(ProcessErrorCode.NOT_FOUND, "Process was not found");
        } catch (ProcessExecutionStartException e) {
            return RestResponse.error(ProcessErrorCode.FAILED_TO_START, "Process execution has failed to start");
        } catch (IOException e) {
            return RestResponse.error(BasicErrorCode.IO_ERROR, "An unknown error has occurred");
        } catch (InvalidProcessDefinitionException ex) {
            return RestResponse.error(ex.getErrors());
        } catch (ApplicationException ex) {
            return RestResponse.error(ex.toError());
        }catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return RestResponse.error(BasicErrorCode.UNKNOWN, "An unknown error has occurred");
        }
    }

}
