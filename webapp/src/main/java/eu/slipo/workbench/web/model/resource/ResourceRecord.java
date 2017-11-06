package eu.slipo.workbench.web.model.resource;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.slipo.workbench.web.model.EnumDataFormat;
import eu.slipo.workbench.web.model.EnumResourceType;

/**
 * A resource
 */
public class ResourceRecord {

    private long id;

    private long version;

    @JsonDeserialize(using = EnumResourceType.Deserializer.class)
    private EnumResourceType type;

    @JsonDeserialize(using = EnumDataSource.Deserializer.class)
    private EnumDataSource dataSource;

    @JsonDeserialize(using = EnumDataFormat.Deserializer.class)
    private EnumDataFormat inputFormat;

    @JsonDeserialize(using = EnumDataFormat.Deserializer.class)
    private EnumDataFormat outputFormat;

    private long processExecutionId;

    private ResourceMetadataView metadata;

    private ZonedDateTime createdOn;

    private ZonedDateTime updatedOn;

    private String fileName;

    private int fileSize;

    private UUID tableName;

    private List<ResourceRecord> versions = new ArrayList<ResourceRecord>();

    public ResourceRecord(long id, int version) {
        this.id = id;
        this.version = version;
    }

    public long getId() {
        return id;
    }

    public long getVersion() {
        return version;
    }

    public EnumResourceType getType() {
        return type;
    }

    public void setType(EnumResourceType type) {
        this.type = type;
    }

    public EnumDataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(EnumDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public EnumDataFormat getInputFormat() {
        return inputFormat;
    }

    public void setInputFormat(EnumDataFormat inputFormat) {
        this.inputFormat = inputFormat;
    }

    public EnumDataFormat getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(EnumDataFormat outputFormat) {
        this.outputFormat = outputFormat;
    }

    public Long getProcessExecutionId() {
        return processExecutionId;
    }

    public void setProcessExecutionId(Long processExecutionId) {
        this.processExecutionId = processExecutionId;
    }

    public ResourceMetadataView getMetadata() {
        return metadata;
    }

    public void setMetadata(ResourceMetadataView metadata) {
        this.metadata = metadata;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public UUID getTable() {
        return tableName;
    }

    public void setTable(UUID tableName) {
        this.tableName = tableName;
    }

    public List<ResourceRecord> getVersions() {
        return Collections.unmodifiableList(this.versions);
    }

    public void addVersion(ResourceRecord r) {
        this.versions.add(r);
    }

}
