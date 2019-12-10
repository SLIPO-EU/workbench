package eu.slipo.workbench.web.model.api.process;

import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.web.model.process.ProcessExecutionRecordView;

public class ProcessExecutionSimpleRecordView {

    private final ProcessSimpleRecord process;

    private final ProcessExecutionSimpleRecord execution;

    public ProcessExecutionSimpleRecordView(ProcessRecord process, ProcessExecutionRecord execution) {
        this.process = new ProcessSimpleRecord(process);
        this.execution = new ProcessExecutionSimpleRecord(execution);
    }

    public ProcessExecutionSimpleRecordView(ProcessExecutionRecordView record) {
        this.process = new ProcessSimpleRecord(record.getProcess());
        if (record.getExecution() != null) {
            this.execution = new ProcessExecutionSimpleRecord(record.getExecution());
        } else {
            this.execution = null;
        }
    }

    public ProcessSimpleRecord getProcess() {
        return process;
    }

    public ProcessExecutionSimpleRecord getExecution() {
        return execution;
    }

}
