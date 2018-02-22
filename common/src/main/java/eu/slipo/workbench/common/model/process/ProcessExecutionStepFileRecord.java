package eu.slipo.workbench.common.model.process;

import java.util.UUID;

import com.vividsolutions.jts.geom.Geometry;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.resource.ResourceIdentifier;

public class ProcessExecutionStepFileRecord 
{
    private long id = -1L;

    private EnumStepFile type;

    private String filePath;

    private Long fileSize;

    private ResourceIdentifier resource;
    
    private EnumDataFormat dataFormat;
    
    private Geometry boundingBox;
    
    private UUID tableName;

    protected ProcessExecutionStepFileRecord() {}
    
    public ProcessExecutionStepFileRecord(EnumStepFile type, String path, Long size)
    {
        this(type, path, size, null);
    }
    
    public ProcessExecutionStepFileRecord(EnumStepFile type, String path)
    {
        this(type, path, null, null);
    }
    
    public ProcessExecutionStepFileRecord(
        EnumStepFile type, String path, Long size, EnumDataFormat format)
    {
        this.type = type;
        this.filePath = path;
        this.fileSize = size;
        this.dataFormat = format;
    }
    
    public ProcessExecutionStepFileRecord(ProcessExecutionStepFileRecord record)
    {
        this.id = record.id;
        this.type = record.type;
        this.filePath = record.filePath;
        this.fileSize = record.fileSize;
        this.resource = record.resource;
        this.dataFormat = record.dataFormat;
        this.boundingBox = record.boundingBox;
        this.tableName = record.tableName;
    }
    
    public long getId() 
    {
        return id;
    }

    public void setId(long id) 
    {
        this.id = id;
    }

    public EnumStepFile getType() 
    {
        return type;
    }

    public void setType(EnumStepFile type) 
    {
        this.type = type;
    }

    public String getFilePath() 
    {
        return filePath;
    }

    public void setFilePath(String path) 
    {
        this.filePath = path;
    }

    public Long getFileSize() 
    {
        return fileSize;
    }

    public void setFileSize(long fileSize) 
    {
        this.fileSize = fileSize;
    }

    public ResourceIdentifier getResource() 
    {
        return resource;
    }
    
    public void setResource(ResourceIdentifier resource) 
    {
        this.resource = resource;
    }

    public void setResource(long id, long version) 
    {
        this.resource = new ResourceIdentifier(id, version);
    }

    public EnumDataFormat getDataFormat()
    {
        return dataFormat;
    }

    public void setDataFormat(EnumDataFormat dataFormat)
    {
        this.dataFormat = dataFormat;
    }

    public void setBoundingBox(Geometry boundingBox)
    {
        this.boundingBox = boundingBox;
    }
    
    public Geometry getBoundingBox()
    {
        return boundingBox;
    }
    
    public void setTableName(UUID tableName)
    {
        this.tableName = tableName;
    }
    
    public UUID getTableName()
    {
        return tableName;
    }

    @Override
    public String toString()
    {
        return String.format(
            "ProcessExecutionStepFileRecord " +
                "[id=%s, type=%s, filePath=%s, fileSize=%s, resource=%s, dataFormat=%s, boundingBox=%s, tableName=%s]",
            id, type, filePath, fileSize, resource, dataFormat, boundingBox, tableName);
    }
    
    
}
