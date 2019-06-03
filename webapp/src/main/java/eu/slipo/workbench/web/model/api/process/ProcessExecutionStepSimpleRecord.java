package eu.slipo.workbench.web.model.api.process;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import eu.slipo.workbench.common.model.poi.EnumOperation;
import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.process.EnumProcessExecutionStatus;
import eu.slipo.workbench.common.model.process.ProcessExecutionStepRecord;

public class ProcessExecutionStepSimpleRecord {

    private int key;
    private String name;
    private EnumProcessExecutionStatus status;
    private EnumTool tool;
    private EnumOperation operation;
    private ZonedDateTime startedOn;
    private ZonedDateTime completedOn;
    private List<ProcessExecutionStepFileSimpleRecord> files;

    public ProcessExecutionStepSimpleRecord(ProcessExecutionStepRecord record) {
        this.key = record.getKey();
        this.name = record.getName();
        this.status = record.getStatus();
        this.tool = record.getTool();
        this.operation = record.getOperation();
        this.startedOn = record.getStartedOn();
        this.completedOn = record.getCompletedOn();

        files = record.getFiles().stream()
            .map(f -> new ProcessExecutionStepFileSimpleRecord(f))
            .collect(Collectors.toList());
    }

    public int getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public EnumProcessExecutionStatus getStatus() {
        return status;
    }

    public EnumTool getTool() {
        return tool;
    }

    public EnumOperation getOperation() {
        return operation;
    }

    public ZonedDateTime getStartedOn() {
        return startedOn;
    }

    public ZonedDateTime getCompletedOn() {
        return completedOn;
    }

    public List<ProcessExecutionStepFileSimpleRecord> getFiles() {
        return files;
    }

}
