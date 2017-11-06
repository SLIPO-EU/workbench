package eu.slipo.workbench.web.model.process;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.slipo.workbench.web.model.UserInfo;

/**
 * A process
 */
public class ProcessRecord {

    private long id;

    private long version;

    private UserInfo createdBy;

    private UserInfo updatedBy;

    private ZonedDateTime createdOn;

    private ZonedDateTime updatedOn;

    private ZonedDateTime executedOn;

    private String name;

    private String description;

    private List<ProcessHistoryRecord> versions = new ArrayList<ProcessHistoryRecord>();

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

    public UserInfo getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserInfo createdBy) {
        this.createdBy = createdBy;
    }

    public UserInfo getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(UserInfo updatedBy) {
        this.updatedBy = updatedBy;
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

    public List<ProcessHistoryRecord> getVersions() {
        return Collections.unmodifiableList(this.versions);
    }

    public void addVersion(ProcessHistoryRecord p) {
        this.versions.add(p);
    }

}
