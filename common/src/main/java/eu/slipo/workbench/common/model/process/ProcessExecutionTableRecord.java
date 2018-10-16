package eu.slipo.workbench.common.model.process;

import java.time.ZonedDateTime;
import java.util.UUID;

import eu.slipo.workbench.common.model.poi.EnumResourceType;

public class ProcessExecutionTableRecord {

    private long id;

    private EnumResourceType type;

    private int outputKey;

    private ZonedDateTime createdOn;

    private UUID tableName;

    public ProcessExecutionTableRecord(long id) {
        this.id = id;
    }

    public ProcessExecutionTableRecord(ProcessExecutionTableRecord record) {
        this.id = record.id;
        this.type = record.type;
        this.outputKey = record.outputKey;
        this.createdOn = record.createdOn;
        this.tableName = record.tableName;
    }

    public long getId() {
        return id;
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

    public UUID getTableName() {
        return tableName;
    }

    public void setTableName(UUID tableName) {
        this.tableName = tableName;
    }

}
