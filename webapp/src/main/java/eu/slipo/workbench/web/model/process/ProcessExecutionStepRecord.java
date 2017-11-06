package eu.slipo.workbench.web.model.process;

import java.time.ZonedDateTime;

import eu.slipo.workbench.web.model.EnumOperation;
import eu.slipo.workbench.web.model.EnumTool;

public class ProcessExecutionStepRecord {

    private int id;

    private EnumTool component;

    private EnumOperation operation;

    private ZonedDateTime startedOn;

    private ZonedDateTime completedOn;

    private String errorMessage;

    public ProcessExecutionStepRecord(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public EnumTool getComponent() {
        return component;
    }

    public void setComponent(EnumTool component) {
        this.component = component;
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

}
