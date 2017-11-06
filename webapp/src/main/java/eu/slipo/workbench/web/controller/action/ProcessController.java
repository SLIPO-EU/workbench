package eu.slipo.workbench.web.controller.action;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.web.model.EnumOperation;
import eu.slipo.workbench.web.model.EnumTool;
import eu.slipo.workbench.web.model.QueryPagingOptions;
import eu.slipo.workbench.web.model.QueryResult;
import eu.slipo.workbench.web.model.UserInfo;
import eu.slipo.workbench.web.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.web.model.process.ProcessExecutionStepRecord;
import eu.slipo.workbench.web.model.process.ProcessHistoryRecord;
import eu.slipo.workbench.web.model.process.ProcessQuery;
import eu.slipo.workbench.web.model.process.ProcessRecord;

/**
 * Actions for managing processes
 */
@RestController
public class ProcessController {

    /**
     * Search for processes
     *
     * @param authentication the authenticated principal
     * @param data the query to execute
     * @return a list of processes
     */
    @RequestMapping(value = "/action/process/query", method = RequestMethod.POST, produces = "application/json")
    public RestResponse<QueryResult<ProcessRecord>> search(Authentication authentication, @RequestBody ProcessQuery data) {
        QueryPagingOptions pagingOptions = data.getPagingOptions();
        if (pagingOptions == null) {
            pagingOptions = new QueryPagingOptions();
            pagingOptions.pageIndex = 0;
            pagingOptions.pageSize = 10;

        } else if (pagingOptions.pageIndex < 0) {
            pagingOptions.pageIndex = 0;
        } else if (pagingOptions.pageSize <= 0) {
            pagingOptions.pageSize = 10;
        }

        int count = (pagingOptions.pageIndex + 1) * pagingOptions.pageSize + (int) (Math.random() * 100);

        QueryResult<ProcessRecord> result = new QueryResult<ProcessRecord>(pagingOptions, count);

        long id = pagingOptions.pageIndex * pagingOptions.pageSize + 1;

        for (int i = 0; i < pagingOptions.pageSize; i++) {
            id += i;
            int versions = (int) (Math.random() * 10F);

            ProcessRecord process = this.createProcess(id, 1);
            for (int j = 0; j < versions; j++) {
                process.addVersion(this.createProcessHistory(id, j + 2));
            }
            result.addItem(process);
        }

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
    public RestResponse<List<ProcessExecutionRecord>> getAllExecutions(Authentication authentication, @PathVariable long id, @PathVariable long version) {
        List<ProcessExecutionRecord> executions = new ArrayList<ProcessExecutionRecord>();

        int count = (int) (Math.random() * 20F);
        for (int j = 0; j < count; j++) {
            executions.add(this.createProcessExecution(j + 1, id, version, false));
        }

        return RestResponse.<List<ProcessExecutionRecord>>result(executions);
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
    public RestResponse<ProcessExecutionRecord> getExecution(
            Authentication authentication,
            @PathVariable long processId,
            @PathVariable long processVersion,
            @PathVariable long executionId) {

        return RestResponse.<ProcessExecutionRecord>result(this.createProcessExecution(executionId, processId, processVersion, true));
    }

    private ProcessRecord createProcess(long id, int version) {
        ProcessRecord process = new ProcessRecord(id, version);

        process.setCreatedBy(createUser());
        process.setCreatedOn(ZonedDateTime.now());
        process.setUpdatedBy(createUser());
        process.setUpdatedOn(ZonedDateTime.now());
        process.setName(String.format("Process %d-%d", id, version));
        process.setDescription("Process description");

        return process;
    }

    private ProcessHistoryRecord createProcessHistory(long id, int version) {
        ProcessHistoryRecord process = new ProcessHistoryRecord(id, version);

        process.setUpdatedBy(createUser());
        process.setUpdatedOn(ZonedDateTime.now());
        process.setName(String.format("Process %d-%d", id, version));
        process.setDescription("Process description");

        return process;
    }

    private ProcessExecutionRecord createProcessExecution(long executionId, long processId, long processVersion, boolean includeSteps) {
        ProcessExecutionRecord execution = new ProcessExecutionRecord(executionId, processId, processVersion);

        execution.setStatus("COMPLETED");
        execution.setStartedOn(ZonedDateTime.now().minusDays(10));
        execution.setCompletedOn(ZonedDateTime.now().minusDays(9));

        if(includeSteps) {
            this.addSteps(execution);
        }

        return execution;
    }

    private void addSteps(ProcessExecutionRecord execution) {
        ProcessExecutionStepRecord step = new ProcessExecutionStepRecord(0);

        step.setComponent(EnumTool.LIMES);
        step.setOperation(EnumOperation.INTERLINK);
        step.setStartedOn(ZonedDateTime.now().minusHours(10));
        step.setCompletedOn(ZonedDateTime.now().minusHours(9));
        execution.addStep(step);

        step = new ProcessExecutionStepRecord(1);

        step.setComponent(EnumTool.FAGI);
        step.setOperation(EnumOperation.FUSION);
        step.setStartedOn(ZonedDateTime.now().minusHours(8));
        step.setCompletedOn(ZonedDateTime.now().minusHours(5));
        execution.addStep(step);

        step = new ProcessExecutionStepRecord(2);

        step.setComponent(EnumTool.DEER);
        step.setOperation(EnumOperation.ENRICHEMENT);
        step.setStartedOn(ZonedDateTime.now().minusHours(4));
        step.setCompletedOn(ZonedDateTime.now().minusHours(1));
        execution.addStep(step);
    }

    private UserInfo createUser() {
        return new UserInfo(1, "Administrator");
    }

}
