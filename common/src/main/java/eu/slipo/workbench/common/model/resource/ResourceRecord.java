package eu.slipo.workbench.common.model.resource;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.vividsolutions.jts.geom.Geometry;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumResourceType;
import eu.slipo.workbench.common.model.user.AccountInfo;

public class ResourceRecord
{
    private long id = -1L;

    private long version = -1L;

    @JsonDeserialize(using = EnumResourceType.Deserializer.class)
    private EnumResourceType type;

    @JsonDeserialize(using = EnumDataSourceType.Deserializer.class)
    private EnumDataSourceType sourceType;

    private EnumDataFormat inputFormat;

    private EnumDataFormat format;

    private Long processExecutionId;

    private ResourceMetadataView metadata;

    private ZonedDateTime createdOn;

    private AccountInfo createdBy;

    private ZonedDateTime updatedOn;

    private AccountInfo updatedBy;

    private String filePath;

    private Long fileSize;

    private UUID tableName;

    private Geometry boundingBox;

    private Integer numberOfEntities;

    private List<ResourceRecord> revisions;

    public ResourceRecord() {}

    public ResourceRecord(long id, long version)
    {
        this.id = id;
        this.version = version;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getVersion()
    {
        return version;
    }

    public void setVersion(long version)
    {
        this.version = version;
    }

    public EnumResourceType getType() {
        return type;
    }

    public void setType(EnumResourceType type) 
    {
        this.type = type;
    }

    public EnumDataSourceType getSourceType()
    {
        return sourceType;
    }

    public void setSourceType(EnumDataSourceType sourceType)
    {
        this.sourceType = sourceType;
    }

    public EnumDataFormat getInputFormat()
    {
        return inputFormat;
    }

    public void setInputFormat(EnumDataFormat inputFormat)
    {
        this.inputFormat = inputFormat;
    }

    public EnumDataFormat getFormat()
    {
        return format;
    }

    public void setFormat(EnumDataFormat format)
    {
        this.format = format;
    }

    public Long getProcessExecutionId()
    {
        return processExecutionId;
    }

    public void setProcessExecutionId(Long processExecutionId)
    {
        this.processExecutionId = processExecutionId;
    }

    public ResourceMetadataView getMetadata()
    {
        return metadata;
    }

    public void setMetadata(ResourceMetadataView metadata)
    {
        this.metadata = metadata;
    }

    public void setMetadata(String name, String description)
    {
        this.metadata = new ResourceMetadataView(name, description);
    }

    @JsonIgnore
    public String getName()
    {
        return metadata == null? null : metadata.getName();
    }
    
    @JsonIgnore
    public String getDescription()
    {
        return metadata == null? null : metadata.getDescription();
    }

    public ZonedDateTime getCreatedOn()
    {
        return createdOn;
    }

    public void setCreatedOn(ZonedDateTime createdOn)
    {
        this.createdOn = createdOn;
    }

    public AccountInfo getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(AccountInfo createdBy)
    {
        this.createdBy = createdBy;
    }

    public void setCreatedBy(int id, String name)
    {
        this.createdBy = new AccountInfo(id, name);
    }

    public ZonedDateTime getUpdatedOn()
    {
        return updatedOn;
    }

    public void setUpdatedOn(ZonedDateTime updatedOn)
    {
        this.updatedOn = updatedOn;
    }

    public AccountInfo getUpdatedBy()
    {
        return updatedBy;
    }

    public void setUpdatedBy(AccountInfo updatedBy) 
    {
        this.updatedBy = updatedBy;
    }

    public void setUpdatedBy(int id, String name)
    {
        this.updatedBy = new AccountInfo(id, name);
    }

    public List<ResourceRecord> getRevisions()
    {
        return revisions == null?
            Collections.emptyList() : Collections.unmodifiableList(revisions);
    }

    public void addRevision(ResourceRecord r)
    {
        if (revisions == null) {
            revisions = new ArrayList<>();
        }
        revisions.add(r);
    }

    public String getFilePath()
    {
        return filePath;
    }

    public void setFilePath(String filePath)
    {
        this.filePath = filePath;
    }

    public Long getFileSize()
    {
        return fileSize;
    }

    public void setFileSize(Long fileSize)
    {
        this.fileSize = fileSize;
    }

    public UUID getTableName()
    {
        return tableName;
    }

    public void setTableName(UUID tableName)
    {
        this.tableName = tableName;
    }
    
    public Geometry getBoundingBox()
    {
        return boundingBox;
    }
    
    public void setBoundingBox(Geometry boundingBox)
    {
        this.boundingBox = boundingBox;
    }
    
    public Integer getNumberOfEntities()
    {
        return numberOfEntities;
    }
    
    public void setNumberOfEntities(Integer numberOfEntities)
    {
        this.numberOfEntities = numberOfEntities;
    }
}
