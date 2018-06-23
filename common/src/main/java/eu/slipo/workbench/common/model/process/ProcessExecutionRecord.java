package eu.slipo.workbench.common.model.process;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.slipo.workbench.common.model.user.AccountInfo;

public class ProcessExecutionRecord implements Serializable
{
    private static final long serialVersionUID = 1L;

    private long id = -1L;

    private String name;

    private AccountInfo submittedBy;

    private ZonedDateTime submittedOn;

    private ProcessIdentifier process;

    private ZonedDateTime startedOn;

    private ZonedDateTime completedOn;

    private EnumProcessExecutionStatus status;

    private EnumProcessTaskType taskType;

    private String errorMessage;

    @JsonIgnore
    private boolean isRunning;

    private List<ProcessExecutionStepRecord> steps;

    public ProcessExecutionRecord() {}

    public ProcessExecutionRecord(long executionId, long processId, long processVersion)
    {
        this.id = executionId;
        this.process = new ProcessIdentifier(processId, processVersion);
    }

    public ProcessExecutionRecord(long executionId, ProcessIdentifier processIdentifier)
    {
        this.id = executionId;
        this.process = new ProcessIdentifier(processIdentifier);
    }

    public ProcessExecutionRecord(ProcessExecutionRecord record)
    {
        this(record, true);
    }

    /**
     * Create a new {@link ProcessExecutionRecord} by copying another record.
     *
     * @param record The record to copy from
     * @param copyDeep A flag to indicate if we should deep copy the step records
     *   from source record.
     */
    public ProcessExecutionRecord(ProcessExecutionRecord record, boolean copyDeep)
    {
        this.id = record.id;
        this.name = record.name;
        this.submittedBy = record.submittedBy;
        this.submittedOn = record.startedOn;
        this.process = record.process;
        this.startedOn = record.startedOn;
        this.completedOn = record.completedOn;
        this.status = record.status;
        this.taskType = record.getTaskType();
        this.errorMessage = record.errorMessage;
        this.steps = copyDeep?
            (record.steps.stream()
                .map(s -> new ProcessExecutionStepRecord(s, true))
                .collect(Collectors.toList())):
            (new ArrayList<>(record.steps));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AccountInfo getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(int id, String name) {
        this.submittedBy = new AccountInfo(id, name);
    }

    public void setSubmittedBy(AccountInfo submittedBy) {
        this.submittedBy = submittedBy;
    }

    public ZonedDateTime getSubmittedOn() {
        return submittedOn;
    }

    public void setSubmittedOn(ZonedDateTime submittedOn) {
        this.submittedOn = submittedOn;
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

    public EnumProcessExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(EnumProcessExecutionStatus status) {
        this.status = status;
    }

    public EnumProcessTaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(EnumProcessTaskType taskType) {
        this.taskType = taskType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @JsonProperty
    public boolean isRunning() {
        return isRunning;
    }

    @JsonIgnore
    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public long getId() {
        return id;
    }

    public ProcessIdentifier getProcess() {
        return process;
    }

    public List<ProcessExecutionStepRecord> getSteps()
    {
        return steps == null?
            Collections.emptyList() : Collections.unmodifiableList(steps);
    }

    public void setSteps(List<ProcessExecutionStepRecord> steps)
    {
        this.steps = new ArrayList(steps);
    }
    
    public void addStep(ProcessExecutionStepRecord s)
    {
        if (this.steps == null) {
            this.steps = new ArrayList<>();
        }
        this.steps.add(s);
    }

    public ProcessExecutionStepRecord getStep(int stepKey)
    {
        if (this.steps == null) {
            return null;
        }
        for (ProcessExecutionStepRecord r: this.steps) {
            if (r.getKey() == stepKey) {
                return r;
            }
        }
        return null;
    }

    public ProcessExecutionStepRecord getStepByName(String name)
    {
        Assert.isTrue(!StringUtils.isEmpty(name), "A non-empty name is required");

        if (this.steps == null) {
            return null;
        }
        for (ProcessExecutionStepRecord r: this.steps) {
            if (r.getName().equals(name)) {
                return r;
            }
        }
        return null;
    }

    public ProcessExecutionStepRecord getStepByNodeName(String nodeName)
    {
        Assert.isTrue(!StringUtils.isEmpty(nodeName), "A non-empty name is required");

        if (this.steps == null) {
            return null;
        }
        for (ProcessExecutionStepRecord r: this.steps) {
            if (r.getNodeName().equals(nodeName)) {
                return r;
            }
        }
        return null;
    }
}
