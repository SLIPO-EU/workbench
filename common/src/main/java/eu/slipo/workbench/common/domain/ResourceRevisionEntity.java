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
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.databind.JsonNode;
import com.vividsolutions.jts.geom.Geometry;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumResourceType;
import eu.slipo.workbench.common.model.process.ProcessExecutionIdentifier;
import eu.slipo.workbench.common.model.resource.EnumDataSourceType;
import eu.slipo.workbench.common.model.resource.ResourceRecord;

@Entity(name = "ResourceRevision")
@Table(
    schema = "public",
    name = "resource_revision",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_resource_parent_id_version", columnNames = { "parent", "`version`" })
    }
)
public class ResourceRevisionEntity {

    @Id
    @Column(name = "id", updatable = false)
    @SequenceGenerator(
        sequenceName = "resource_revision_id_seq", name = "resource_revision_id_seq", initialValue = 1, allocationSize = 1)
    @GeneratedValue(generator = "resource_revision_id_seq", strategy = GenerationType.SEQUENCE)
    long id = -1L;

    @NotNull
    @NaturalId
    @ManyToOne
    @JoinColumn(name = "parent", nullable = false, updatable = false)
    ResourceEntity parent;

    @NotNull
    @NaturalId
    @Column(name = "`version`", nullable = false, updatable = false)
    long version;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "`type`", nullable = false, updatable = false)
    EnumResourceType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, updatable = false)
    EnumDataSourceType sourceType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "input_format", nullable = false, updatable = false)
    EnumDataFormat inputFormat;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false, updatable = false)
    EnumDataFormat format;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_execution")
    ProcessExecutionEntity processExecution;

    @NotBlank
    @Column(name = "`name`", nullable = false, updatable = false)
    String name;

    @NotNull
    @Column(name = "description", updatable = false)
    String description;

    @NotNull
    @Column(name = "updated_on", nullable = false, updatable = false)
    ZonedDateTime updatedOn;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "updated_by", nullable = false, updatable = false)
    AccountEntity updatedBy;

    @Column(name = "bbox")
    Geometry boundingBox;

    @Min(0)
    @Column(name = "number_of_entities")
    Integer numberOfEntities;

    @NotBlank
    @Column(name = "file_path", nullable = false, updatable = false)
    String filePath;

    @Min(0)
    @Column(name = "file_size", updatable = false)
    Long fileSize;

    @Column(name = "table_name", columnDefinition = "uuid")
    UUID tableName;

    @Column(name = "layer_style", updatable = true, nullable = true)
    JsonNode style;

    protected ResourceRevisionEntity() {}

    public ResourceRevisionEntity(ResourceEntity parent)
    {
        this.parent = parent;
        this.version = parent.version;

        this.updatedBy = parent.updatedBy;
        this.updatedOn = parent.updatedOn;
        this.filePath = parent.filePath;
        this.fileSize = parent.fileSize;
        this.name = parent.name;
        this.description = parent.description;
        this.inputFormat = parent.inputFormat;
        this.format = parent.format;
        this.type = parent.type;
        this.sourceType = parent.sourceType;
        this.processExecution = parent.processExecution;
        this.tableName = parent.tableName;
        this.numberOfEntities = parent.numberOfEntities;
        this.boundingBox = parent.boundingBox;
    }

    public long getId()
    {
        return id;
    }

    public ResourceEntity getParent()
    {
        return parent;
    }

    public void setParent(ResourceEntity parent)
    {
        this.parent = parent;
    }

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

    public EnumDataSourceType getSourceType()
    {
        return sourceType;
    }

    public EnumDataFormat getInputFormat()
    {
        return inputFormat;
    }

    public EnumDataFormat getFormat()
    {
        return format;
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

    public String getDescription()
    {
        return description;
    }

    public ZonedDateTime getUpdatedOn()
    {
        return updatedOn;
    }

    public AccountEntity getUpdatedBy()
    {
        return updatedBy;
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

    public Long getFileSize()
    {
        return fileSize;
    }

    public UUID getTableName()
    {
        return tableName;
    }

    public void setTableName(UUID tableName)
    {
        this.tableName = tableName;
    }

    public JsonNode getStyle()
    {
        return style;
    }

    public void setStyle(JsonNode style)
    {
        this.style = style;
    }

    public ResourceRecord toResourceRecord()
    {
        ResourceRecord record = new ResourceRecord(parent.id, version);

        record.setCreatedOn(parent.createdOn);
        record.setCreatedBy(parent.createdBy.getId(), parent.createdBy.getFullName());
        record.setUpdatedOn(updatedOn);
        record.setUpdatedBy(updatedBy.getId(), updatedBy.getFullName());

        record.setType(type);
        record.setSourceType(sourceType);
        record.setInputFormat(inputFormat);
        record.setFormat(format);
        record.setFilePath(filePath);
        record.setFileSize(fileSize);
        record.setMetadata(name, description);
        record.setTableName(tableName);
        record.setBoundingBox(boundingBox);
        record.setNumberOfEntities(numberOfEntities);
        record.setStyle(style);

        if (processExecution != null) {
            record.setExecution(
                ProcessExecutionIdentifier.of(
                    processExecution.getProcess().getParent().getId(),
                    processExecution.getProcess().getVersion(),
                    processExecution.getId())
            );
            ProcessExecutionMapExportEntity map = processExecution.getMap();
            if (map != null) {
                record.setExportedBy(map.getCreatedBy().getId(), map.getCreatedBy().getFullName());
                record.setExportedOn(map.getCreatedOn());
                record.setExportStatus(map.getStatus());
            }
        }

        return record;
    }
}
