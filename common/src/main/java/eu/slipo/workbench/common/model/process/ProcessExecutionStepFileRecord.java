package eu.slipo.workbench.common.model.process;

import eu.slipo.workbench.common.model.resource.ResourceIdentifier;

public class ProcessExecutionStepFileRecord 
{
    private long id = -1L;

    private EnumStepFile type;

    private String filePath;

    private Long fileSize;

    private ResourceIdentifier resource;

    protected ProcessExecutionStepFileRecord() {}
    
    public ProcessExecutionStepFileRecord(EnumStepFile type, String path, Long size)
    {
        this.type = type;
        this.filePath = path;
        this.fileSize = size;
    }
    
    public ProcessExecutionStepFileRecord(EnumStepFile type, String path)
    {
        this(type, path, null);
    }
    
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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String path) {
        this.filePath = path;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
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
