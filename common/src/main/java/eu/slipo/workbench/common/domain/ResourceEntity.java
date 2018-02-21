package eu.slipo.workbench.common.domain;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
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
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.vividsolutions.jts.geom.Geometry;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumResourceType;
import eu.slipo.workbench.common.model.resource.EnumDataSourceType;
import eu.slipo.workbench.common.model.resource.ResourceMetadataView;
import eu.slipo.workbench.common.model.resource.ResourceRecord;


@Entity(name = "Resource")
@Table(schema = "public", name = "resource")
public class ResourceEntity 
{
    @Id
    @Column(name = "id", updatable = false)
    @SequenceGenerator(
        sequenceName = "resource_id_seq", name = "resource_id_seq", initialValue = 1, allocationSize = 1)
    @GeneratedValue(generator = "resource_id_seq", strategy = GenerationType.SEQUENCE)
    long id = -1L;

    @NotNull
    @Column(name = "`version`", nullable = false)
    long version;

    @Version
    @Column(name = "row_version")
    long rowVersion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "`type`", nullable = false, updatable = false)
    EnumResourceType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    EnumDataSourceType sourceType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "input_format", nullable = false)
    EnumDataFormat inputFormat;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false)
    EnumDataFormat format;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_execution")
    ProcessExecutionEntity processExecution;

    @NotBlank
    @Column(name = "`name`", nullable = false)
    String name;

    @Column(name = "description")
    String description;

    @NotNull
    @Column(name = "created_on", nullable = false, updatable = false)
    ZonedDateTime createdOn;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    AccountEntity createdBy;

    @NotNull
    @Column(name = "updated_on", nullable = false)
    ZonedDateTime updatedOn;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", nullable = false)
    AccountEntity updatedBy;

    @Column(name = "bbox")
    Geometry boundingBox;

    @Min(0)
    @Column(name = "number_of_entities")
    Integer numberOfEntities;

    @NotBlank
    @Column(name = "file_path", nullable = false)
    String filePath;

    @Min(0)
    @Column(name = "file_size")
    Long fileSize;

    @Column(name = "table_name")
    UUID tableName;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<ResourceRevisionEntity> revisions = new ArrayList<>();

    public ResourceEntity() {}    
    
    public long getVersion()
    {
        return version;
    }

    public void setVersion(long version)
    {
        this.version = version;
    }

    public EnumResourceType getType()
    {
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

    public ProcessExecutionEntity getProcessExecution()
    {
        return processExecution;
    }

    public void setProcessExecution(ProcessExecutionEntity processExecution)
    {
        this.processExecution = processExecution;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public ZonedDateTime getCreatedOn()
    {
        return createdOn;
    }

    public void setCreatedOn(ZonedDateTime createdOn)
    {
        this.createdOn = createdOn;
    }

    public AccountEntity getCreatedBy()
    {
        return createdBy;
    }

    public void setCreatedBy(AccountEntity createdBy)
    {
        this.createdBy = createdBy;
    }

    public ZonedDateTime getUpdatedOn()
    {
        return updatedOn;
    }

    public void setUpdatedOn(ZonedDateTime updatedOn)
    {
        this.updatedOn = updatedOn;
    }

    public AccountEntity getUpdatedBy()
    {
        return updatedBy;
    }

    public void setUpdatedBy(AccountEntity updatedBy)
    {
        this.updatedBy = updatedBy;
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

    public long getId()
    {
        return this.id;
    }

    public List<ResourceRevisionEntity> getRevisions() 
    {
        return this.revisions;
    }

    public void addRevision(ResourceRevisionEntity revisionEntity)
    {
        revisionEntity.setParent(this);
        this.revisions.add(revisionEntity);
    }

    public void setMetadata(ResourceMetadataView metadata)
    {
        this.name = metadata.getName();
        this.description = metadata.getDescription();
        this.boundingBox = metadata.getBoundingBox();
    }
    
    public ResourceRecord toResourceRecord()
    {
        return toResourceRecord(true);
    }
    
    public ResourceRecord toResourceRecord(boolean includeRevisions)
    {
        ResourceRecord r = new ResourceRecord(id, version);

        r.setCreatedOn(createdOn);
        r.setCreatedBy(createdBy.getId(), createdBy.getFullName());
        r.setUpdatedOn(updatedOn);
        r.setUpdatedBy(updatedBy.getId(), updatedBy.getFullName());
        r.setSourceType(sourceType);
        r.setInputFormat(inputFormat);
        r.setFormat(format);
        r.setFilePath(filePath);
        r.setFileSize(fileSize);
        r.setMetadata(name, description, numberOfEntities, boundingBox);
        r.setProcessExecutionId(processExecution != null ? processExecution.getId() : null);
        r.setTableName(tableName);
        r.setType(type);

        if (includeRevisions) {
            revisions.stream()
                .sorted(Comparator.comparingLong(ResourceRevisionEntity::getVersion))
                .forEach((h) -> r.addRevision(h.toResourceRecord()));
        }
        
        return r;
    }

}
