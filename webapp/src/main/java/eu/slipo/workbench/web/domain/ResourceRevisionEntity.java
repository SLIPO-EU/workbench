package eu.slipo.workbench.web.domain;

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

import com.vividsolutions.jts.geom.Geometry;

import eu.slipo.workbench.common.domain.AccountEntity;
import eu.slipo.workbench.web.model.EnumDataFormat;
import eu.slipo.workbench.web.model.EnumResourceType;
import eu.slipo.workbench.web.model.resource.EnumDataSourceType;
import eu.slipo.workbench.web.model.resource.ResourceRecord;

@Entity(name = "ResourceRevision")
@Table(
    schema = "public",
    name = "resource_revision",
    uniqueConstraints = { 
        @UniqueConstraint(name = "uq_resource_parent_id_version", columnNames = { "parent", "`version`" }), }
)
public class ResourceRevisionEntity {

    @Id
    @Column(name = "id")
    @SequenceGenerator(
        sequenceName = "resource_revision_id_seq", name = "resource_revision_id_seq", initialValue = 1, allocationSize = 1)
    @GeneratedValue(generator = "resource_revision_id_seq", strategy = GenerationType.SEQUENCE)
    long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "parent", nullable = false)
    ResourceEntity parent;

    @Column(name = "`version`")
    long version;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "`type`")
    EnumResourceType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type")
    EnumDataSourceType sourceType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "input_format")
    EnumDataFormat inputFormat;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "output_format")
    EnumDataFormat outputFormat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_execution")
    ProcessExecutionEntity processExecution;

    @NotNull
    @Column(name = "`name`")
    String name;

    @NotNull
    @Column(name = "description")
    String description;

    @NotNull
    @Column(name = "updated_on")
    ZonedDateTime updatedOn;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "updated_by", nullable = false)
    AccountEntity updatedBy;

    @Column(name = "bbox")
    Geometry boundingBox;

    @Column(name = "number_of_entities")
    Integer numberOfEntities;

    @Column(name = "file_path")
    String path;

    @Column(name = "file_size")
    Long size;

    @Column(name = "table_name")
    UUID tableName;

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

    public ResourceRecord toResourceRecord() {
        ResourceRecord r = new ResourceRecord(this.parent.id, this.version);

        r.setCreatedOn(this.parent.createdOn);
        r.setCreatedBy(this.parent.createdBy.getId(), this.parent.createdBy.getFullName());
        r.setUpdatedOn(this.updatedOn);
        r.setUpdatedBy(this.updatedBy.getId(), this.updatedBy.getFullName());
        r.setDataSource(this.sourceType);
        r.setInputFormat(this.inputFormat);
        r.setOutputFormat(this.outputFormat);
        r.setFilePath(this.path);
        r.setFileSize(this.size);
        r.setMetadata(this.name, this.description, this.numberOfEntities, this.boundingBox);
        r.setProcessExecutionId(this.processExecution.getId());
        r.setTable(this.tableName);
        r.setType(this.type);

        return r;
    }
}
