package eu.slipo.workbench.web.controller.action;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.remoting.RemoteConnectFailureException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.slipo.workbench.common.model.ApplicationException;
import eu.slipo.workbench.common.model.BasicErrorCode;
import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.common.model.FileSystemErrorCode;
import eu.slipo.workbench.common.model.QueryResultPage;
import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.model.process.CatalogResource;
import eu.slipo.workbench.common.model.process.EnumInputType;
import eu.slipo.workbench.common.model.process.EnumProcessTaskType;
import eu.slipo.workbench.common.model.process.EnumStepFile;
import eu.slipo.workbench.common.model.process.InvalidProcessDefinitionException;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessErrorCode;
import eu.slipo.workbench.common.model.process.ProcessExecutionQuery;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStartException;
import eu.slipo.workbench.common.model.process.ProcessExecutionStepFileRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStepRecord;
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
import eu.slipo.workbench.web.service.ProcessService;

/**
 * Actions for managing processes
 */
@RestController
@Secured({ "ROLE_USER", "ROLE_ADMIN" })
@RequestMapping(produces = "application/json")
public class ProcessController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ProcessController.class);

    @Autowired
    private Path catalogDataDirectory;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ProcessRepository processRepository;

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
        query.setTemplate(false);
        query.setCreatedBy(currentUserId());

        PageRequest pageRequest = request.getPageRequest();
        QueryResultPage<ProcessRecord> r = processRepository.query(query, pageRequest);

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
        query.setTaskType(EnumProcessTaskType.DATA_INTEGRATION);
        query.setTemplate(true);
        query.setCreatedBy(currentUserId());

        PageRequest pageRequest = request.getPageRequest();
        QueryResultPage<ProcessRecord> r = processRepository.query(query, pageRequest);

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
        query.setCreatedBy(currentUserId());

        PageRequest pageRequest = request.getPageRequest();
        QueryResultPage<ProcessExecutionRecord> r = processRepository.queryExecutions(query, pageRequest);

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
            ProcessRecord processRecord = processRepository.findOne(id, version, false);
            ProcessExecutionRecord executionRecord = processRepository.findExecution(executionId);
            if (processRecord == null ||
                executionRecord == null ||
                executionRecord.getProcess().getId() != id ||
                executionRecord.getProcess().getVersion() != version) {
                return RestResponse.error(new Error(ProcessErrorCode.NOT_FOUND, "Execution was not found"));
            }

            final Optional<Pair<ProcessExecutionStepRecord, ProcessExecutionStepFileRecord>> result = executionRecord
                .getSteps()
                .stream()
                .flatMap(s -> {
                    return s.getFiles()
                        .stream()
                        .map(f-> Pair.<ProcessExecutionStepRecord, ProcessExecutionStepFileRecord>of(s, f));
                })
                .filter(f -> f.getRight().getId() == fileId)
                .findFirst();

            if (!result.isPresent()) {
                return RestResponse.error(new Error(BasicErrorCode.NO_RESULT, "File was not found"));
            }
            final ProcessExecutionStepRecord stepRecord = result.get().getLeft();
            final ProcessExecutionStepFileRecord fileRecord = result.get().getRight();
            if (fileRecord.getType() != EnumStepFile.KPI) {
                return RestResponse.error(new Error(BasicErrorCode.NOT_SUPPORTED, "File type is not supported"));
            }

            final String filename = fileRecord.getFilePath();
            final Path path = Paths.get(catalogDataDirectory.toString(), filename);
            final File file = path.toFile();

            if (!file.exists()) {
                return RestResponse.error(new Error(FileSystemErrorCode.PATH_NOT_FOUND, "File was not found"));
            }

            switch (stepRecord.getTool()) {
                case TRIPLEGEO:
                    JsonNode node = objectMapper.readTree(file);
                    return RestResponse.result(node);
                default:
                    Resource resource = new FileSystemResource(file);
                    Properties props = PropertiesLoaderUtils.loadProperties(resource);
                    return RestResponse.result(props);
            }
        } catch (JsonParseException ex) {
            return RestResponse.error(new Error(BasicErrorCode.UNKNOWN, "Failed to parse file"));
        } catch (IOException ex) {
            return RestResponse.error(new Error(BasicErrorCode.IO_ERROR, "Failed to read file"));
        }
    }

    /**
     * Downloads a file for the selected execution
     *
     * @param id the process id
     * @param version the process version
     * @param executionId the execution id
     * @param fileId the file id
     * @return a list of {@link ProcessExecutionRecord}
     * @throws IOException  if process or file is not found
     */
    @RequestMapping(value = "/action/process/{id}/{version}/execution/{executionId}/file/{fileId}", method = RequestMethod.GET)
    public FileSystemResource downloadProcessExecutionFile(
        @PathVariable long id, @PathVariable long version, @PathVariable long executionId, @PathVariable long fileId,
        HttpServletResponse response) throws IOException {

        ProcessRecord processRecord = processRepository.findOne(id, version, false);
        ProcessExecutionRecord executionRecord = processRepository.findExecution(executionId);
        if (processRecord == null ||
            executionRecord == null ||
            executionRecord.getProcess().getId() != id ||
            executionRecord.getProcess().getVersion() != version) {

            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Process execution was not found");
            return null;
        }

        final Optional<ProcessExecutionStepFileRecord> result = executionRecord
            .getSteps()
            .stream()
            .flatMap(s -> s.getFiles().stream())
            .filter(f -> f.getId() == fileId)
            .findFirst();

        if (!result.isPresent()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "File was not found");
            return null;
        }

        final String filename = result.get().getFilePath();
        final Path path = Paths.get(catalogDataDirectory.toString(), filename);
        final File file = path.toFile();

        if (!file.exists()) {
            response.sendError(HttpServletResponse.SC_GONE, "File has been removed");
            return null;
        }

        response.setHeader("Content-Disposition", String.format("attachment; filename=%s", file.getName()));

        return new FileSystemResource(file);
    }

    /**
     * Loads the most recent version of an existing process
     *
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
    @RequestMapping(value = "/action/process/{id}/start", method = RequestMethod.POST)
    public RestResponse<?> start(@PathVariable long id) {
        try {
            final ProcessRecord processRecord = this.processRepository.findOne(id);
            if (processRecord == null) {
                return RestResponse.error(new Error(ProcessErrorCode.NOT_FOUND, "Process was not found"));
            }
            this.processService.start(processRecord.getId(), processRecord.getVersion());
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
        ProcessRecord record = null;

        try {
            final ProcessDefinition definition = request.getDefinition();
            if (definition == null) {
                return RestResponse.error(BasicErrorCode.INPUT_INVALID, "No process definition");
            }
            final ProcessDefinition normalizedDefinition = ProcessDefinition.normalize(definition);

            final boolean isTemplate = request.getAction() == EnumProcessSaveActionType.SAVE_TEMPLATE;

            if (id == null) {
                record = processService.create(normalizedDefinition, isTemplate);
            } else {
                record = processService.update(id, normalizedDefinition, isTemplate);
            }
        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }

        try {
            if (request.getAction() == EnumProcessSaveActionType.SAVE_AND_EXECUTE) {
                this.processService.start(record.getId(), record.getVersion());
            }
        } catch (Exception ex) {
            return this.exceptionToResponse(ex, Error.EnumLevel.WARN);
        }

        return RestResponse.result(new ProcessRecordView(record));
    }

    private RestResponse<?> exceptionToResponse(Exception ex) {
        return exceptionToResponse(ex, Error.EnumLevel.ERROR);
    }

    private RestResponse<?> exceptionToResponse(Exception ex, Error.EnumLevel level) {
        if (ex instanceof IOException) {
            return RestResponse.error(BasicErrorCode.IO_ERROR, "An unknown error has occurred", level);
        }

        if (ex instanceof ProcessNotFoundException) {
            return RestResponse.error(ProcessErrorCode.NOT_FOUND, "Process was not found", level);
        }
        if (ex instanceof ProcessExecutionStartException) {
            return RestResponse.error(ProcessErrorCode.FAILED_TO_START, "Process execution has failed to start", level);
        }

        if (ex instanceof InvalidProcessDefinitionException) {
            InvalidProcessDefinitionException typedEx = (InvalidProcessDefinitionException) ex;
            return RestResponse.error(typedEx.getErrors());
        }
        if (ex instanceof ApplicationException) {
            ApplicationException typedEx = (ApplicationException) ex;
            return RestResponse.error(typedEx.toError());
        }
        if (ex instanceof RemoteConnectFailureException) {
            return RestResponse.error(ProcessErrorCode.RPC_SERVER_UNREACHABLE, "Process execution has failed to start. RPC server is unreachable", level);
        }

        logger.error(ex.getMessage(), ex);
        return RestResponse.error(BasicErrorCode.UNKNOWN, "An unknown error has occurred", level);
    }

}
