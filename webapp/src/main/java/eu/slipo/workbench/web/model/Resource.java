package eu.slipo.workbench.web.model;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Resource {

    private long id;

    private int version;

    private EnumResourceType type;

    private EnumDataSource source;

    private Long jobExecutionId;

    private Long stepExecutionId;

    private ResourceMetadata metadata;

    private ZonedDateTime createdOn;

    private ZonedDateTime updatedOn;

    private FileSystemEntry file;

    private UUID table;

    private List<Resource> versions = new ArrayList<Resource>();

    public Resource(long id, int version) {
        this.id = id;
        this.version = version;
    }

    public long getId() {
        return id;
    }

    public int getVersion() {
        return version;
    }

    public EnumResourceType getType() {
        return type;
    }

    public void setType(EnumResourceType type) {
        this.type = type;
    }

    public EnumDataSource getSource() {
        return source;
    }

    public void setSource(EnumDataSource source) {
        this.source = source;
    }

    public Long getJobExecutionId() {
        return jobExecutionId;
    }

    public void setJobExecutionId(Long jobExecutionId) {
        this.jobExecutionId = jobExecutionId;
    }

    public Long getStepExecutionId() {
        return stepExecutionId;
    }

    public void setStepExecutionId(Long stepExecutionId) {
        this.stepExecutionId = stepExecutionId;
    }

    public ResourceMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ResourceMetadata metadata) {
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

    public FileSystemEntry getFile() {
        return file;
    }

    public void setFile(FileSystemEntry file) {
        this.file = file;
    }

    public UUID getTable() {
        return table;
    }

    public void setTable(UUID table) {
        this.table = table;
    }

    public List<Resource> getVersions() {
        return Collections.unmodifiableList(this.versions);
    }

    public void addVersion(Resource r) {
        this.versions.add(r);
    }

}
