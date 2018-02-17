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
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;

import com.vividsolutions.jts.geom.Geometry;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumResourceType;
import eu.slipo.workbench.common.model.resource.EnumDataSourceType;
import eu.slipo.workbench.common.model.resource.ResourceRecord;

@Entity(name = "ResourceRevision")
@Table(
    schema = "public",
    name = "resource_revision",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_resource_parent_id_version", columnNames = { "parent", "`version`" }), }
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
    @Column(name = "output_format", nullable = false, updatable = false)
    EnumDataFormat outputFormat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_execution", updatable = false)
    ProcessExecutionEntity processExecution;

    @NotNull
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

    @Column(name = "bbox", updatable = false)
    Geometry boundingBox;

    @Column(name = "number_of_entities", updatable = false)
    Integer numberOfEntities;

    @Column(name = "file_path", updatable = false)
    String path;

    @Column(name = "file_size", updatable = false)
    Long size;

    @Column(name = "table_name", updatable = false)
    UUID tableName;

    protected ResourceRevisionEntity() {}
    
    public ResourceRevisionEntity(ResourceEntity parent)
    {
        this.parent = parent;
    }
    
    public ResourceEntity getParent() {
        return parent;
    }

    public void setParent(ResourceEntity parent) {
        this.parent = parent;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public EnumResourceType getType() {
        return type;
    }

    public void setType(EnumResourceType type) {
        this.type = type;
    }

    public EnumDataSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(EnumDataSourceType sourceType) {
        this.sourceType = sourceType;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ZonedDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(ZonedDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }

    public AccountEntity getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(AccountEntity updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Geometry getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(Geometry boundingBox) {
        this.boundingBox = boundingBox;
    }

    public Integer getNumberOfEntities() {
        return numberOfEntities;
    }

    public void setNumberOfEntities(Integer numberOfEntities) {
        this.numberOfEntities = numberOfEntities;
    }

    public String getFileName() {
        return path;
    }

    public void setFileName(String fileName) {
        this.path = fileName;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(long fileSize) {
        this.size = fileSize;
    }

    public UUID getTableName() {
        return tableName;
    }

    public void setTableName(UUID tableName) {
        this.tableName = tableName;
    }

    public long getId() {
        return id;
    }

    public ResourceRecord toResourceRecord()
    {
        ResourceRecord r = new ResourceRecord(parent.id, version);

        r.setCreatedOn(parent.createdOn);
        r.setCreatedBy(parent.createdBy.getId(), parent.createdBy.getFullName());
        r.setUpdatedOn(updatedOn);
        r.setUpdatedBy(updatedBy.getId(), updatedBy.getFullName());
        r.setDataSource(sourceType);
        r.setInputFormat(inputFormat);
        r.setOutputFormat(outputFormat);
        r.setFilePath(path);
        r.setFileSize(size);
        r.setMetadata(name, description, numberOfEntities, boundingBox);
        r.setProcessExecutionId(processExecution != null ? processExecution.getId() : null);
        r.setTable(tableName);
        r.setType(type);

        return r;
    }
}
