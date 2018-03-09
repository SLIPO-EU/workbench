package eu.slipo.workbench.common.model.process;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.slipo.workbench.common.model.user.AccountInfo;

public class ProcessRecord
{
    // Todo: represent identity-related numbers as Long ??

    private long id = -1L;

    private long version = -1L;

    private AccountInfo createdBy;

    private AccountInfo updatedBy;

    private ZonedDateTime createdOn;

    private ZonedDateTime updatedOn;

    private ZonedDateTime executedOn;

    private String name;

    private String description;

    private ProcessDefinition definition;

    private boolean template;

    private EnumProcessTaskType taskType;

    private List<ProcessRecord> revisions;

    private List<ProcessExecutionRecord> executions;

    public ProcessRecord() {}

    public ProcessRecord(long id, long version) {
        this.id = id;
        this.version = version;
    }

    public long getId() {
        return id;
    }

    public long getVersion() {
        return version;
    }

    public AccountInfo getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int id, String name) {
        this.createdBy = new AccountInfo(id, name);
    }

    public void setCreatedBy(AccountInfo createdBy) {
        this.createdBy = createdBy;
    }

    public AccountInfo getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(AccountInfo updatedBy) {
        this.updatedBy = updatedBy;
    }

    public void setUpdatedBy(int id, String name) {
        this.updatedBy = new AccountInfo(id, name);
    }

    public ZonedDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(ZonedDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public ZonedDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(ZonedDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }

    public ZonedDateTime getExecutedOn() {
        return executedOn;
    }

    public void setExecutedOn(ZonedDateTime executedOn) {
        this.executedOn = executedOn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProcessDefinition getDefinition()
    {
        return definition;
    }

    public void setDefinition(ProcessDefinition definition)
    {
        this.definition = definition;
    }

    public boolean isTemplate() {
        return template;
    }

    public void setTemplate(boolean template) {
        this.template = template;
    }

    public EnumProcessTaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(EnumProcessTaskType t) {
        this.taskType = t;
    }

    public List<ProcessRecord> getRevisions()
    {
        return this.revisions == null?
            Collections.emptyList() : Collections.unmodifiableList(this.revisions);
    }

    public void addRevision(ProcessRecord p)
    {
        if (this.revisions == null) {
            this.revisions = new ArrayList<>();
        }
        this.revisions.add(p);
    }

    public List<ProcessExecutionRecord> getExecutions()
    {
        return this.executions == null?
            Collections.emptyList() : Collections.unmodifiableList(this.executions);
    }

    public void addExecution(ProcessExecutionRecord e)
    {
        if (this.executions == null) {
            this.executions = new ArrayList<>();
        }
        this.executions.add(e);
    }

    @Override
    public String toString()
    {
        return String.format(
            "ProcessRecord [id=%s, version=%s, name=%s]", id, version, name);
    }


}
