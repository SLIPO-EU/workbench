package eu.slipo.workbench.common.model.process;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.slipo.workbench.common.model.poi.EnumOperation;
import eu.slipo.workbench.common.model.poi.EnumTool;

public class ProcessExecutionStepRecord {

    private long id = -1L;

    private int key;

    private String name;

    private long jobExecutionId;
    
    private EnumProcessExecutionStatus status;

    private EnumTool tool;

    private EnumOperation operation;

    private ZonedDateTime startedOn;

    private ZonedDateTime completedOn;

    private String errorMessage;

    private List<ProcessExecutionStepFileRecord> files = new ArrayList<>(2);

    public ProcessExecutionStepRecord() {}
    
    public ProcessExecutionStepRecord(long id, int key, String name) 
    {
        this.id = id;
        this.key = key;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public int getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public long getJobExecutionId()
    {
        return jobExecutionId;
    }

    public void setJobExecutionId(long jobExecutionId)
    {
        this.jobExecutionId = jobExecutionId;
    }
    
    public EnumProcessExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(EnumProcessExecutionStatus status) {
        this.status = status;
    }

    public EnumTool getTool() {
        return tool;
    }

    public void setTool(EnumTool tool) {
        this.tool = tool;
    }

    public EnumOperation getOperation() {
        return operation;
    }

    public void setOperation(EnumOperation operation) {
        this.operation = operation;
    }

    public ZonedDateTime getStartedOn() {
        return startedOn;
    }

    public void setStartedOn(ZonedDateTime startedOn) {
        this.startedOn = startedOn;
    }

    public ZonedDateTime getCompletedOn() {
        return completedOn;
    }

    public void setCompletedOn(ZonedDateTime completedOn) {
        this.completedOn = completedOn;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<ProcessExecutionStepFileRecord> getFiles() {
        return Collections.unmodifiableList(files);
    }

    public void addFile(ProcessExecutionStepFileRecord f) {
        this.files.add(f);
    }
}
