package eu.slipo.workbench.common.domain;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
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
import javax.validation.constraints.NotNull;

import eu.slipo.workbench.common.domain.attributeconverter.ProcessConfigurationConverter;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessRecord;


@Entity(name = "ProcessRevision")
@Table(
    schema = "public",
    name = "process_revision",
    uniqueConstraints = { 
        @UniqueConstraint(name = "uq_process_parent_id_version", columnNames = { "parent", "`version`" }), }
)
public class ProcessRevisionEntity {

    @Id
    @Column(name = "id")
    @SequenceGenerator(
        sequenceName = "process_revision_id_seq", name = "process_revision_id_seq", initialValue = 1, allocationSize = 1)
    @GeneratedValue(generator = "process_revision_id_seq", strategy = GenerationType.SEQUENCE)
    long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "parent", nullable = false)
    ProcessEntity parent;

    @Column(name = "`version`")
    long version;

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

    @Column(name = "executed_on")
    ZonedDateTime executedOn;

    @NotNull
    @Column(name = "definition")
    @Convert(converter = ProcessConfigurationConverter.class)
    ProcessDefinition definition;

    @OneToMany(mappedBy = "process", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<ProcessExecutionEntity> executions = new ArrayList<>();

    public ProcessEntity getParent() {
        return parent;
    }

    public void setParent(ProcessEntity parent) {
        this.parent = parent;
    }

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

    public ProcessDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(ProcessDefinition definition) {
        this.definition = definition;
    }

    public long getId() {
        return id;
    }

    public List<ProcessExecutionEntity> getExecutions() {
        return executions;
    }

    public ProcessRecord toProcessRecord()
    {
        return toProcessRecord(false, false);
    }
    
    public ProcessRecord toProcessRecord(boolean includeExecutions, boolean includeSteps) 
    {
        ProcessRecord p = new ProcessRecord(this.parent.id, this.version);

        p.setCreatedOn(this.parent.createdOn);
        p.setCreatedBy(this.parent.createdBy.getId(), this.parent.createdBy.getFullName());
        p.setUpdatedOn(this.updatedOn);
        p.setUpdatedBy(this.updatedBy.getId(), this.updatedBy.getFullName());
        p.setDescription(this.description);
        p.setName(this.name);
        p.setExecutedOn(this.executedOn);
        p.setTaskType(this.parent.getTaskType());
        p.setConfiguration(this.definition);
        p.setTemplate(this.parent.isTemplate);

        if (includeExecutions) {
            for (ProcessExecutionEntity e : this.getExecutions()) {
                p.addExecution(e.toProcessExecutionRecord(includeSteps));
            }
        }

        return p;
    }
}
