package eu.slipo.workbench.web.domain;

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

import eu.slipo.workbench.common.domain.AccountEntity;
import eu.slipo.workbench.web.model.EnumDataFormat;
import eu.slipo.workbench.web.model.EnumResourceType;
import eu.slipo.workbench.web.model.resource.EnumDataSource;
import eu.slipo.workbench.web.model.resource.ResourceRecord;

@Entity(name = "Resource")
@Table(
    schema = "public",
    name = "resource",
    uniqueConstraints = { @UniqueConstraint(name = "uq_resource_id_version", columnNames = { "id", "`version`" }), }
)
public class ResourceEntity {

    @Id
    @Column(name = "id")
    @SequenceGenerator(sequenceName = "resource_id_seq", name = "resource_id_seq", initialValue = 1, allocationSize = 1)
    @GeneratedValue(generator = "resource_id_seq", strategy = GenerationType.SEQUENCE)
    long id;

    @Column(name = "`version`")
    long version;

    @Version()
    @Column(name = "row_version")
    long rowVersion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "`type`")
    private EnumResourceType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "data_source")
    private EnumDataSource dataSource;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "input_format")
    private EnumDataFormat inputFormat;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "output_format")
    private EnumDataFormat outputFormat;

    @Column(name = "process_execution_id")
    long processExecutionId;

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
    private Geometry boundingBox;

    @Column(name = "number_of_entities")
    Integer numberOfEntities;

    @Column(name = "file_name")
    String fileName;

    @Column(name = "file_size")
    int fileSize;

    @Column(name = "table_name")
    UUID tableName;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<ResourceRevisionEntity> versions = new ArrayList<>();

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

    public EnumDataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(EnumDataSource dataSource) {
        this.dataSource = dataSource;
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

    public long getProcessExecutionId() {
        return processExecutionId;
    }

    public void setProcessExecutionId(long processExecutionId) {
        this.processExecutionId = processExecutionId;
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
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
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

    public List<ResourceRevisionEntity> getVersions() {
        return versions;
    }

    public ResourceRecord toResourceRecord() {
        ResourceRecord r = new ResourceRecord(this.id, this.version);

        r.setCreatedOn(this.createdOn);
        r.setCreatedBy(this.createdBy.getId(), this.createdBy.getFullName());
        r.setUpdatedOn(this.updatedOn);
        r.setUpdatedBy(this.updatedBy.getId(), this.updatedBy.getFullName());
        r.setDataSource(this.dataSource);
        r.setInputFormat(this.inputFormat);
        r.setOutputFormat(this.outputFormat);
        r.setFileName(this.fileName);
        r.setFileSize(this.fileSize);
        r.setMetadata(this.name, this.description, this.numberOfEntities, this.boundingBox);
        r.setProcessExecutionId(this.processExecutionId);
        r.setTable(this.tableName);
        r.setType(this.type);

        this.getVersions()
            .stream()
            .sorted((h1, h2) -> Long.compare(h2.getVersion(), h1.getVersion()))
            .forEach((h) -> r.addVersion(h.toResourceRecord()));

        return r;
    }

}
