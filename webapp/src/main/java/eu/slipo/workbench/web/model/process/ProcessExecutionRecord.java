package eu.slipo.workbench.web.model.process;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProcessExecutionRecord {

    private long id;

    private ProcessIdentifier process;

    private ZonedDateTime startedOn;

    private ZonedDateTime completedOn;

    private String status;

    private String errorMessage;

    private List<ProcessExecutionStepRecord> steps = new ArrayList<ProcessExecutionStepRecord>();

    public ProcessExecutionRecord(long executionId, long processId, long processVersion) {
        this.id = executionId;
        this.process = new ProcessIdentifier(processId, processVersion);
    }

    public ProcessExecutionRecord(long id, ProcessIdentifier process) {
        this.id = id;
        this.process = process.clone();
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getId() {
        return id;
    }

    public ProcessIdentifier getProcess() {
        return process;
    }

    public List<ProcessExecutionStepRecord> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    public void addStep(ProcessExecutionStepRecord s) {
        this.steps.add(s);
    }

}
