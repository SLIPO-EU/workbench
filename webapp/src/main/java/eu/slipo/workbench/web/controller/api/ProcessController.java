package eu.slipo.workbench.web.controller.api;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.common.model.QueryResultPage;
import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.model.process.EnumProcessTaskType;
import eu.slipo.workbench.common.model.process.ProcessErrorCode;
import eu.slipo.workbench.common.model.process.ProcessExecutionFileNotFoundException;
import eu.slipo.workbench.common.model.process.ProcessExecutionNotFoundException;
import eu.slipo.workbench.common.model.process.ProcessNotFoundException;
import eu.slipo.workbench.common.model.process.ProcessQuery;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.web.model.QueryResult;
import eu.slipo.workbench.web.model.api.process.ProcessExecutionSimpleRecordView;
import eu.slipo.workbench.web.model.api.process.ProcessSimpleRecord;
import eu.slipo.workbench.web.model.process.ProcessExecutionRecordView;
import eu.slipo.workbench.web.model.process.ProcessQueryRequest;
import eu.slipo.workbench.web.service.ProcessService;

@Secured({ "ROLE_API" })
@RestController("ApiProcessController")
@RequestMapping(produces = "application/json")
public class ProcessController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ProcessController.class);

    @Autowired
    private ProcessService processService;

    /**
     * Query user workflows
     *
     * @param request A query for filtering workflows
     * @return A list of {@link ProcessSimpleRecord}
     */
    @PostMapping(value = "/api/v1/process")
    public RestResponse<?> query(@RequestBody ProcessQueryRequest request) {
        try {
            if (request == null || request.getQuery() == null) {
                return RestResponse.error(ProcessErrorCode.QUERY_IS_EMPTY, "The query is empty");
            }

            ProcessQuery query = request.getQuery();
            query.setCreatedBy(this.currentUserId());
            query.setTemplate(false);
            query.setExcludeApi(false);

            PageRequest pageRequest = request.getPageRequest();

            QueryResultPage<ProcessRecord> result = this.processService.find(query, pageRequest);

            return RestResponse.result(new QueryResult<ProcessSimpleRecord>(
                request.getPagingOptions(),
                result.getCount(),
                result.getItems().stream().map(p -> new ProcessSimpleRecord(p)).collect(Collectors.toList())
            ));

        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }
    }

    /**
     * Get workflow execution instance status
     *
     * @param id The workflow id
     * @param version The workflow version
     * @return an instance of {@link ProcessExecutionSimpleRecordView}
     */
    @CrossOrigin
    @Secured({ "ROLE_API", "ROLE_API_SESSION" })
    @GetMapping(value = "/api/v1/process/{id}/{version}")
    public RestResponse<?> getStatus(
        @PathVariable long id, @PathVariable long version
    ) {
        try {
            ProcessExecutionRecordView result = this.processService.getProcessExecution(id, version);
            if (result == null) {
               return RestResponse.error(ProcessErrorCode.PROCESS_NOT_FOUND, "Process was not found");
            }

            return RestResponse.result(new ProcessExecutionSimpleRecordView(result));
        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }
    }

    /**
     * Get workflow execution instance file
     *
     * @param id The workflow id
     * @param version The workflow version
     * @param fileId The file id
     * @param response The HTTP response
     * @return An instance of {@link FileSystemResource} if file exists; Otherwise a
     * server error is returned
     *
     * @throws IOException If an I/O error has occurred
     */
    @GetMapping(value = "/api/v1/process/{id}/{version}/file/{fileId}")
    public FileSystemResource getFile(
        @PathVariable long id, @PathVariable long version, @PathVariable long fileId,
        HttpServletResponse response
    ) throws IOException {

        final File file;
        try {
            ProcessExecutionRecordView result = this.processService.getProcessExecution(id, version);
            if (result == null) {
                createErrorResponse(
                    HttpServletResponse.SC_NOT_FOUND, response, ProcessErrorCode.EXECUTION_NOT_FOUND, "Process execution was not found"
                );
                return null;
            }

            file = this.processService.getProcessExecutionFile(id, version, result.getExecution().getId(), fileId);

            if ((file != null) && (file.exists())) {
                response.setHeader("Content-Disposition", String.format("attachment; filename=%s", file.getName()));
                return new FileSystemResource(file);
            } else {
                createErrorResponse(
                    HttpServletResponse.SC_NOT_FOUND, response, ProcessErrorCode.FILE_NOT_FOUND, "Process execution file was not found"
                );
            }
        } catch (ProcessNotFoundException ex) {
            createErrorResponse(
                HttpServletResponse.SC_NOT_FOUND, response, ProcessErrorCode.PROCESS_NOT_FOUND, "Process was not found"
            );
        } catch (ProcessExecutionNotFoundException ex) {
            createErrorResponse(
                HttpServletResponse.SC_NOT_FOUND, response, ProcessErrorCode.EXECUTION_NOT_FOUND, "Process execution was not found"
            );
        } catch (ProcessExecutionFileNotFoundException ex) {
            createErrorResponse(
                HttpServletResponse.SC_NOT_FOUND, response, ProcessErrorCode.EXECUTION_NOT_FOUND, "File was not found"
            );
        }

        return null;
    }

    /**
     * Creates a new version for the workflow with the specified id
     *
     * @param id The workflow id
     *
     * @return A instance of {@link ProcessSimpleRecord} if the operation was successful;
     * Otherwise an error message is returned
     */
    @PostMapping(value = "/api/v1/process/{id}/save")
    public RestResponse<?> save(@PathVariable long id) {
        try {
            ProcessRecord process = this.processService.findOne(id);
            if (process == null) {
                throw new ProcessNotFoundException(id);
            }
            process = this.processService.update(id, process.getDefinition(), false);

            return this.getStatus(id, process.getVersion());
        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }
    }

    /**
     * Starts a workflow revision execution
     *
     * @param id The workflow id
     * @param version The workflow version
     *
     * @return An empty instance of {@link RestResponse} if operation was successful;
     * Otherwise an error message is returned
     */
    @PostMapping(value = "/api/v1/process/{id}/{version}/start")
    public RestResponse<?> start(@PathVariable long id, @PathVariable long version) {
        try {
            ProcessRecord process = this.processService.findOne(id, version);
            if (process == null) {
                throw new ProcessNotFoundException(id, version);
            }
            this.processService.start(id, version, EnumProcessTaskType.UNDEFINED);

            return this.getStatus(id, version);
        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }
    }

    /**
     * Stops a workflow revision execution
     *
     * @param id The workflow id
     * @param version The workflow version
     *
     * @return An empty instance of {@link RestResponse} if operation was successful;
     * Otherwise an error message is returned
     */
    @PostMapping(value = "/api/v1/process/{id}/{version}/stop")
    public RestResponse<?> stop(@PathVariable long id, @PathVariable long version) {
        try {
            ProcessRecord process = this.processService.findOne(id, version);
            if (process == null) {
                throw new ProcessNotFoundException(id, version);
            }
            this.processService.stop(id, version);
        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }
        return RestResponse.success();
    }

    private RestResponse<?> exceptionToResponse(Exception ex) {
        logger.error(ex.getMessage(), ex);
        return exceptionToResponse(ex, Error.EnumLevel.ERROR);
    }

}
