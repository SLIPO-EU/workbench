package eu.slipo.workbench.common.model.process;

import java.time.ZonedDateTime;

import eu.slipo.workbench.common.model.poi.EnumOperation;
import eu.slipo.workbench.common.model.security.ApplicationKeyRecord;
import eu.slipo.workbench.common.model.user.AccountInfo;

public class ProcessExecutionApiRecord {

    private long id;
    private ProcessExecutionRecord execution;
    private ApplicationKeyRecord key;
    private EnumOperation operation;

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public long getProcessId() {
        return this.execution.getProcess().getId();
    }

    public long getProcessVersion() {
        return this.execution.getProcess().getVersion();
    }

    public void setExecution(ProcessExecutionRecord execution) {
        this.execution = execution;
    }

    public void setKey(ApplicationKeyRecord key) {
        this.key = key;
    }

    public EnumOperation getOperation() {
        return operation;
    }

    public void setOperation(EnumOperation operation) {
        this.operation = operation;
    }

    public String getName() {
        return this.key.getName();
    }

    public AccountInfo getSubmittedBy() {
        return this.execution.getSubmittedBy();
    }

    public ZonedDateTime getSubmittedOn() {
        return this.execution.getSubmittedOn();
    }

    public ZonedDateTime getStartedOn() {
        return this.execution.getStartedOn();
    }

    public ZonedDateTime getCompletedOn() {
        return this.execution.getCompletedOn();
    }

    public EnumProcessExecutionStatus getStatus() {
        return this.execution.getStatus();
    }

    public EnumProcessTaskType getTaskType() {
        return this.execution.getTaskType();
    }

    public String getErrorMessage() {
        return this.execution.getErrorMessage();
    }

    public boolean isRunning() {
        return this.execution.isRunning();
    }

    public void setRunning(boolean isRunning) {
        this.execution.setRunning(isRunning);
    }

    public String getApplicationName() {
        return this.key.getName();
    }

}
