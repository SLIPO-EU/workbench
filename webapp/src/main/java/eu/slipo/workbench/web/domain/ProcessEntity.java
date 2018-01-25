package eu.slipo.workbench.web.domain;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
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

import eu.slipo.workbench.common.domain.AccountEntity;
import eu.slipo.workbench.web.model.process.EnumProcessTask;
import eu.slipo.workbench.web.model.process.ProcessDefinitionUpdate;
import eu.slipo.workbench.web.model.process.ProcessRecord;

@Entity(name = "Process")
@Table(
    schema = "public",
    name = "process",
    uniqueConstraints = { @UniqueConstraint(name = "uq_process_id_version", columnNames = { "id", "`version`" }), }
)
public class ProcessEntity {

    @Id
    @Column(name = "id")
    @SequenceGenerator(sequenceName = "process_id_seq", name = "process_id_seq", initialValue = 1, allocationSize = 1)
    @GeneratedValue(generator = "process_id_seq", strategy = GenerationType.SEQUENCE)
    long id;

    @Column(name = "`version`")
    long version;

    @Version()
    @Column(name = "row_version")
    long rowVersion;

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

    @Column(name = "executed_on")
    ZonedDateTime executedOn;

    @NotNull
    @Basic()
    @Convert(converter = ProcessConfigurationConverter.class)
    ProcessDefinitionUpdate configuration;

    @NotNull
    @Basic()
    boolean template;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "task")
    private EnumProcessTask task;

    @OneToMany(mappedBy = "process", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<ProcessRevisionEntity> versions = new ArrayList<>();

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
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

    public void setCreatedOn(ZonedDateTime createdOn) {
        this.createdOn = createdOn;
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

    public ZonedDateTime getExecutedOn() {
        return executedOn;
    }

    public void setExecutedOn(ZonedDateTime executedOn) {
        this.executedOn = executedOn;
    }

    public ProcessDefinitionUpdate getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ProcessDefinitionUpdate configuration) {
        this.configuration = configuration;
    }

    public boolean isTemplate() {
        return template;
    }

    public void setTemplate(boolean template) {
        this.template = template;
    }

    public EnumProcessTask getTask() {
        return task;
    }

    public void setTask(EnumProcessTask task) {
        this.task = task;
    }

    public long getId() {
        return id;
    }

    public long getRowVersion() {
        return rowVersion;
    }

    public void setCreatedBy(AccountEntity createdBy) {
        this.createdBy = createdBy;
    }

    public AccountEntity getCreatedBy() {
        return createdBy;
    }

    public List<ProcessRevisionEntity> getVersions() {
        return versions;
    }

    public ProcessRecord toProcessRecord(boolean includeExecutions, boolean includeSteps) {
        ProcessRecord p = new ProcessRecord(this.id, this.version);

        p.setCreatedOn(this.createdOn);
        p.setCreatedBy(this.createdBy.getId(), this.createdBy.getFullName());
        p.setUpdatedOn(this.updatedOn);
        p.setUpdatedBy(this.updatedBy.getId(), this.updatedBy.getFullName());
        p.setDescription(this.description);
        p.setName(this.name);
        p.setExecutedOn(this.executedOn);
        p.setTask(this.task);
        p.setConfiguration(this.configuration);
        p.setTemplate(this.template);

        for (ProcessRevisionEntity h : this.getVersions()) {
            p.addVersion(h.toProcessRecord(includeExecutions, includeSteps));
        }

        return p;
    }

}
