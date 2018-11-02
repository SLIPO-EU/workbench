package eu.slipo.workbench.common.domain;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import eu.slipo.workbench.common.model.poi.EnumResourceType;
import eu.slipo.workbench.common.model.process.ProcessExecutionTableRecord;


@Entity(name = "ProcessExecutionTable")
@Table(schema = "public", name = "process_execution_table")
public class ProcessExecutionTableEntity
{
    @Id
    @Column(name = "id", updatable = false)
    @SequenceGenerator(
        sequenceName = "process_execution_table_id_seq", name = "process_execution_table_id_seq", initialValue = 1, allocationSize = 1)
    @GeneratedValue(generator = "process_execution_table_id_seq", strategy = GenerationType.SEQUENCE)
    long id = -1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_execution")
    ProcessExecutionEntity execution;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "`type`", nullable = false, updatable = false)
    EnumResourceType type;

    @NotNull
    @Column(name = "output_key", nullable = false, updatable = false)
    int outputKey;

    @NotNull
    @Column(name = "created_on", nullable = false, updatable = false)
    ZonedDateTime createdOn;

    @Column(name = "table_name", columnDefinition = "uuid")
    UUID tableName = UUID.randomUUID();

    public ProcessExecutionTableEntity() {
    }

    public ProcessExecutionEntity getExecution() {
        return execution;
    }

    public void setExecution(ProcessExecutionEntity execution) {
        this.execution = execution;
    }

    public EnumResourceType getType() {
        return type;
    }

    public void setType(EnumResourceType type) {
        this.type = type;
    }

    public int getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(int outputKey) {
        this.outputKey = outputKey;
    }

    public ZonedDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(ZonedDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public long getId() {
        return id;
    }

    public UUID getTableName() {
        return tableName;
    }

    public ProcessExecutionTableRecord toProcessExecutionTableRecord() {
        ProcessExecutionTableRecord record = new ProcessExecutionTableRecord(id);

        record.setCreatedOn(createdOn);
        record.setOutputKey(outputKey);
        record.setTableName(tableName);
        record.setType(type);

        return record;
    }

}
