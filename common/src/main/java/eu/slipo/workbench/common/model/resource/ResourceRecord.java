package eu.slipo.workbench.common.model.resource;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.vividsolutions.jts.geom.Geometry;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumResourceType;
import eu.slipo.workbench.common.model.user.Account;

public class ResourceRecord {

    private long id = -1L;

    private long version;

    @JsonDeserialize(using = EnumResourceType.Deserializer.class)
    private EnumResourceType type;

    @JsonDeserialize(using = EnumDataSourceType.Deserializer.class)
    private EnumDataSourceType dataSource;

    private EnumDataFormat inputFormat;

    private EnumDataFormat outputFormat;

    private Long processExecutionId;

    private ResourceMetadataView metadata;

    private ZonedDateTime createdOn;

    private Account createdBy;

    private ZonedDateTime updatedOn;

    private Account updatedBy;

    private String fileName;

    private Long fileSize;

    private UUID tableName;

    private List<ResourceRecord> revisions = new ArrayList<ResourceRecord>();

    public ResourceRecord(long id, long version) {
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

    public EnumDataSourceType getDataSource() {
        return dataSource;
    }

    public void setDataSource(EnumDataSourceType dataSource) {
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

    public void setMetadata(String name, String description, Integer size, Geometry boundingBox) {
        this.metadata = new ResourceMetadataView(name, description, size, boundingBox);
    }

    public ZonedDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(ZonedDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public Account getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Account createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreatedBy(int id, String name) {
        this.createdBy = new Account(id, name, null);
    }

    public ZonedDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(ZonedDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }

    public Account getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Account updatedBy) {
        this.updatedBy = updatedBy;
    }

    public void setUpdatedBy(int id, String name) {
        this.updatedBy = new Account(id, name, null);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFilePath(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public UUID getTable() {
        return tableName;
    }

    public void setTable(UUID tableName) {
        this.tableName = tableName;
    }

    public List<ResourceRecord> getRevisions() {
        return Collections.unmodifiableList(this.revisions);
    }

    public void addRevision(ResourceRecord r) {
        this.revisions.add(r);
    }
}
