package eu.slipo.workbench.common.domain;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;

import eu.slipo.workbench.common.model.etl.EnumMapExportStatus;
import eu.slipo.workbench.common.model.etl.MapExportTask;


@Entity(name = "ProcessExecutionMapExport")
@Table(
    schema = "public", name = "process_execution_map_export"
)
public class ProcessExecutionMapExportEntity
{
    @Id
    @Column(name = "id", updatable = false)
    @SequenceGenerator(
        sequenceName = "process_execution_map_export_id_seq", name = "process_execution_map_export_id_seq", initialValue = 1, allocationSize = 1)
    @GeneratedValue(generator = "process_execution_map_export_id_seq", strategy = GenerationType.SEQUENCE)
    private long id = -1L;

    @NotNull
    @NaturalId
    @OneToOne
    @JoinColumn(name = "execution_workflow", nullable = false, updatable = false)
    private ProcessExecutionEntity workflow;

    @OneToOne
    @JoinColumn(name = "execution_transform")
    private ProcessExecutionEntity transform;

    @NotNull
    @Column(name = "created_on", nullable = false, updatable = false)
    private ZonedDateTime createdOn = ZonedDateTime.now();

    @NotNull
    @ManyToOne()
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    private AccountEntity createdBy;

    @Column(name = "started_on")
    private ZonedDateTime startedOn;

    @Column(name = "completed_on")
    private ZonedDateTime completedOn;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EnumMapExportStatus status = EnumMapExportStatus.PENDING;

    public ProcessExecutionEntity getWorkflow() {
        return workflow;
    }

    public void setWorkflow(ProcessExecutionEntity workflow) {
        this.workflow = workflow;
    }

    public ProcessExecutionEntity getTransform() {
        return transform;
    }

    public void setTransform(ProcessExecutionEntity transform) {
        this.transform = transform;
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

    public long getId() {
        return id;
    }

    public ZonedDateTime getCreatedOn() {
        return createdOn;
    }

    public AccountEntity getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(AccountEntity createdBy) {
        this.createdBy = createdBy;
    }

    public MapExportTask toRecord() {
        MapExportTask record = new MapExportTask(
            this.id, this.createdOn, this.createdBy.toAccountInfo(), this.workflow.toProcessExecutionRecord()
        );

        record.setStartedOn(startedOn);
        record.setCompletedOn(completedOn);
        record.setStatus(status);
        record.setTransform(transform != null ? transform.toProcessExecutionRecord() : null);

        return record;
    }

}
