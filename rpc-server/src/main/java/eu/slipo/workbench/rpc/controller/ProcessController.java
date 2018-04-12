package eu.slipo.workbench.rpc.controller;

import java.util.List;

import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.workbench.common.model.ApplicationException;
import eu.slipo.workbench.common.model.BasicErrorCode;
import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStartException;
import eu.slipo.workbench.common.model.process.ProcessExecutionStopException;
import eu.slipo.workbench.common.model.process.ProcessIdentifier;
import eu.slipo.workbench.common.model.process.ProcessNotFoundException;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.common.repository.ProcessRepository;
import eu.slipo.workbench.common.service.ProcessOperator;

@RestController
@RequestMapping(produces = "application/json")
public class ProcessController
{
    private static final Logger logger = LoggerFactory.getLogger(ProcessController.class);

    @Autowired
    private ProcessOperator processOperator;

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private Validator validator;

    @PostMapping(value = "/api/proc/start")
    public RestResponse<ProcessExecutionRecord> startProc(
        @RequestParam("id") Long id, @RequestParam("version") Long version)
        throws Exception
    {
        ProcessExecutionRecord executionRecord = null;
        String errorMessage = null;
        try {
            executionRecord = processOperator.start(id, version);
        } catch (ProcessExecutionStartException | ProcessNotFoundException ex) {
            executionRecord = null;
            errorMessage = ex.getMessage();
        }

        if (executionRecord != null) {
            logger.info("Submitted as execution #{}", executionRecord.getId());
            return RestResponse.result(executionRecord);
        } else {
            return RestResponse.error(BasicErrorCode.NO_RESULT, errorMessage);
        }
    }

    @PostMapping(value = "/api/proc/stop")
    public RestResponse<ProcessExecutionRecord> stopProc(
        @RequestParam("id") Long id, @RequestParam("version") Long version)
    {
        String errorMessage = null;
        try {
            processOperator.stop(id, version);
        } catch (ProcessNotFoundException | ProcessExecutionStopException ex) {
            errorMessage = ex.getMessage();
        }

        if (errorMessage != null)
            return RestResponse.error(BasicErrorCode.NO_RESULT, errorMessage);

        logger.info("Requested from execution of process #{} (version={}) to stop", id, version);

        ProcessExecutionRecord executionRecord = processOperator.poll(id, version);
        return RestResponse.result(executionRecord);
    }

    @GetMapping(value = "/api/proc/status")
    public RestResponse<ProcessExecutionRecord> statusProc(
        @RequestParam("id") Long id, @RequestParam("version") Long version)
    {
        ProcessRecord processRecord = processRepository.findOne(id, version);
        if (processRecord == null)
            throw ApplicationException.fromPattern(
                BasicErrorCode.NO_RESULT, "The given (id,version) did not match a process");

        ProcessExecutionRecord executionRecord = processOperator.poll(id, version);
        return RestResponse.result(executionRecord);
    }

    @GetMapping(value = "/api/proc/list")
    public RestResponse<List<ProcessIdentifier>> listProc()
    {
        return RestResponse.result(processOperator.list(true));
    }
}
