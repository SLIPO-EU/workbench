package eu.slipo.workbench.common.domain;

import java.time.ZonedDateTime;
import java.util.ArrayList;
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
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import com.vividsolutions.jts.geom.Geometry;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumResourceType;
import eu.slipo.workbench.common.model.resource.EnumDataSourceType;
import eu.slipo.workbench.common.model.resource.ResourceRecord;


@Entity(name = "Resource")
@Table(schema = "public", name = "resource")
public class ResourceEntity {

    @Id
    @Column(name = "id")
    @SequenceGenerator(
        sequenceName = "resource_id_seq", name = "resource_id_seq", initialValue = 1, allocationSize = 1)
    @GeneratedValue(generator = "resource_id_seq", strategy = GenerationType.SEQUENCE)
    long id = -1L;

    @Column(name = "`version`")
    long version;

    @Version()
    @Column(name = "row_version")
    long rowVersion;

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
    @Column(name = "created_on")
    ZonedDateTime createdOn = ZonedDateTime.now();

    @NotNull
    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    AccountEntity createdBy;

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

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<ResourceRevisionEntity> revisions = new ArrayList<>();

    public ResourceEntity() {
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

    public ZonedDateTime getCreatedOn() {
        return createdOn;
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

    public void setFileName(String path) {
        this.path = path;
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

    public long getRowVersion() {
        return rowVersion;
    }

    public AccountEntity getCreatedBy() {
        return createdBy;
    }

    public List<ResourceRevisionEntity> getRevisions() {
        return revisions;
    }

    public void addRevision(ResourceRevisionEntity revisionEntity)
    {
        revisions.add(revisionEntity);
    }
    
    public ResourceRecord toResourceRecord() 
    {
        ResourceRecord r = new ResourceRecord(id, version);

        r.setCreatedOn(createdOn);
        r.setCreatedBy(createdBy.getId(), createdBy.getFullName());
        r.setUpdatedOn(updatedOn);
        r.setUpdatedBy(updatedBy.getId(), updatedBy.getFullName());
        r.setDataSource(sourceType);
        r.setInputFormat(inputFormat);
        r.setOutputFormat(outputFormat);
        r.setFilePath(path);
        r.setFileSize(size);
        r.setMetadata(name, description, numberOfEntities, boundingBox);
        r.setProcessExecutionId(processExecution.getId());
        r.setTable(tableName);
        r.setType(type);

        revisions.stream()
            .sorted((h1, h2) -> Long.compare(h2.getVersion(), h1.getVersion()))
            .forEach((h) -> r.addRevision(h.toResourceRecord()));

        return r;
    }

}