package eu.slipo.workbench.web.model.api.process;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import eu.slipo.workbench.common.model.process.EnumProcessExecutionStatus;
import eu.slipo.workbench.common.model.process.EnumProcessTaskType;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;

public class ProcessExecutionSimpleRecord {

    private EnumProcessExecutionStatus status;
    private EnumProcessTaskType taskType;
    private long id;
    private long processId;
    private long processVersion;
    private String name;
    private ZonedDateTime completedOn;
    private ZonedDateTime startedOn;
    private ZonedDateTime submittedOn;
    private List<ProcessExecutionStepSimpleRecord> steps;

    public ProcessExecutionSimpleRecord() {

    }

    public ProcessExecutionSimpleRecord(ProcessExecutionRecord record) {
        this.status = record.getStatus();
        this.taskType = record.getTaskType();
        this.id = record.getId();
        this.processId = record.getProcess().getId();
        this.processVersion = record.getProcess().getVersion();
        this.name = record.getName();
        this.completedOn = record.getCompletedOn();
        this.startedOn = record.getStartedOn();
        this.submittedOn = record.getSubmittedOn();

        this.steps = record.getSteps().stream()
            .map(s -> new ProcessExecutionStepSimpleRecord(s))
            .collect(Collectors.toList());
    }

    public EnumProcessExecutionStatus getStatus() {
        return status;
    }

    public EnumProcessTaskType getTaskType() {
        return taskType;
    }

    public long getId() {
        return id;
    }

    public long getProcessId() {
        return processId;
    }

    public long getProcessVersion() {
        return processVersion;
    }

    public String getName() {
        return name;
    }

    public ZonedDateTime getCompletedOn() {
        return completedOn;
    }

    public ZonedDateTime getStartedOn() {
        return startedOn;
    }

    public ZonedDateTime getSubmittedOn() {
        return submittedOn;
    }

    public List<ProcessExecutionStepSimpleRecord> getSteps() {
        return steps;
    }

}
