package eu.slipo.workbench.web.model.process;

import java.util.UUID;

import eu.slipo.workbench.web.model.resource.ResourceIdentifier;

public class ProcessExecutionStepFileRecord {

    private long id;

    private EnumStepFile type;

    private String fileName;

    private Long fileSize;

    private UUID tableName;

    private ResourceIdentifier resource;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public EnumStepFile getType() {
        return type;
    }

    public void setType(EnumStepFile type) {
        this.type = type;
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

    public UUID getTableName() {
        return tableName;
    }

    public void setTableName(UUID tableName) {
        this.tableName = tableName;
    }

    public ResourceIdentifier getResource() {
        return resource;
    }

    public void setResource(ResourceIdentifier resource) {
        this.resource = resource;
    }

    public void setResource(long id, long version) {
        this.resource = new ResourceIdentifier(id, version);
    }

}
