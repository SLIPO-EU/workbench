package eu.slipo.workbench.web.model.process;

import java.io.Serializable;

import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessRecord;

public class ProcessExecutionRecordView implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ProcessRecord process;

    private final ProcessExecutionRecord execution;

    public ProcessExecutionRecordView(ProcessRecord processRecord, ProcessExecutionRecord executionRecord) {
        this.process = processRecord;
        this.execution = executionRecord;
    }

    public ProcessRecord getProcess() {
        return process;
    }

    public ProcessExecutionRecord getExecution() {
        return execution;
    }

}
