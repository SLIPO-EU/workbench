package eu.slipo.workbench.common.model.etl;

import java.time.ZonedDateTime;

import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.user.AccountInfo;

public class MapExportTask {

    private long id;

    private ZonedDateTime createdOn;

    private AccountInfo createdBy;

    private ZonedDateTime startedOn;

    private ZonedDateTime completedOn;

    private EnumMapExportStatus status = EnumMapExportStatus.PENDING;

    private ProcessExecutionRecord workflow;

    private ProcessExecutionRecord transform;

    public MapExportTask(long id, ZonedDateTime createdOn, AccountInfo createdBy, ProcessExecutionRecord workflow) {
        this.id = id;
        this.createdOn = createdOn;
        this.createdBy = createdBy;
        this.workflow = workflow;
    }

    public long getId() {
        return id;
    }

    public ZonedDateTime getCreatedOn() {
        return createdOn;
    }

    public AccountInfo getCreatedBy() {
        return createdBy;
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

    public EnumMapExportStatus getStatus() {
        return status;
    }

    public void setStatus(EnumMapExportStatus status) {
        this.status = status;
    }

    public ProcessExecutionRecord getWorkflow() {
        return workflow;
    }

    public ProcessExecutionRecord getTransform() {
        return transform;
    }

    public void setTransform(ProcessExecutionRecord transform) {
        this.transform = transform;
    }

}
