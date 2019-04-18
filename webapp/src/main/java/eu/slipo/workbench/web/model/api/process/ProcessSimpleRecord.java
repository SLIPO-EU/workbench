package eu.slipo.workbench.web.model.api.process;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import eu.slipo.workbench.common.model.process.EnumProcessTaskType;
import eu.slipo.workbench.common.model.process.ProcessRecord;

public class ProcessSimpleRecord {

    private ZonedDateTime createdOn;
    private String description;
    private ZonedDateTime executedOn;
    private long id;
    private String name;
    private List<ProcessSimpleRecord> revisions = new ArrayList<>();
    private EnumProcessTaskType taskType;
    private ZonedDateTime updatedOn;
    private long version;

    public ProcessSimpleRecord(ProcessRecord record) {
        this.createdOn = record.getCreatedOn();
        this.description = record.getDescription();
        this.executedOn = record.getExecutedOn();
        this.id = record.getId();
        this.name = record.getName();
        this.taskType = record.getTaskType();
        this.updatedOn = record.getUpdatedOn();
        this.version = record.getVersion();

        for (ProcessRecord p : record.getRevisions()) {
            this.revisions.add(new ProcessSimpleRecord(p));
        }
    }

    public long getId() {
        return id;
    }

    public long getVersion() {
        return version;
    }

    public ZonedDateTime getCreatedOn() {
        return createdOn;
    }

    public ZonedDateTime getUpdatedOn() {
        return updatedOn;
    }

    public ZonedDateTime getExecutedOn() {
        return executedOn;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public EnumProcessTaskType getTaskType() {
        return taskType;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<ProcessSimpleRecord> getRevisions() {
        return this.revisions == null ? Collections.emptyList() : Collections.unmodifiableList(this.revisions);
    }

}
