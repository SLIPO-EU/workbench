package eu.slipo.workbench.common.domain;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import eu.slipo.workbench.common.domain.attributeconverter.ProcessDefinitionConverter;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessIdentifier;
import eu.slipo.workbench.common.model.process.ProcessRecord;


@Entity(name = "ProcessRevision")
@Table(
    schema = "public",
    name = "process_revision",
    uniqueConstraints = { 
        @UniqueConstraint(name = "uq_process_parent_id_version", columnNames = { "parent", "`version`" })
    }
)
public class ProcessRevisionEntity {

    @Id
    @Column(name = "id", updatable = false)
    @SequenceGenerator(
        sequenceName = "process_revision_id_seq", name = "process_revision_id_seq", initialValue = 1, allocationSize = 1)
    @GeneratedValue(generator = "process_revision_id_seq", strategy = GenerationType.SEQUENCE)
    long id = -1L;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "parent", nullable = false, updatable = false)
    ProcessEntity parent;

    @NotNull
    @Column(name = "`version`", nullable = false, updatable = false)
    long version;

    @NotNull
    @Column(name = "`name`", nullable = false)
    String name;

    @Column(name = "description", updatable = false)
    String description;

    @NotNull
    @Column(name = "updated_on", nullable = false, updatable = false)
    ZonedDateTime updatedOn;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", nullable = false, updatable = false)
    AccountEntity updatedBy;

    @NotNull
    @Column(name = "definition", nullable = false, updatable = false, length = 4096)
    @Convert(converter = ProcessDefinitionConverter.class)
    ProcessDefinition definition;

    @OneToMany(
        mappedBy = "process", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<ProcessExecutionEntity> executions = new ArrayList<>();

    @OneToOne(
        mappedBy = "process", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    ProcessExecutionMonitorEntity monitor;
        
    protected ProcessRevisionEntity() {}
    
    public ProcessRevisionEntity(ProcessEntity parent)
    {
        this.parent = parent;
        this.version = parent.version;
            
        this.name = parent.name;
        this.description = parent.description;
        this.definition = parent.definition;
        this.updatedBy = parent.updatedBy;
        this.updatedOn = parent.updatedOn;
    }
    
    public long getId()
    {
        return id;
    }

    public ProcessEntity getParent()
    {
        return parent;
    }
    
    public void setParent(ProcessEntity parent)
    {
        this.parent = parent;
    }

    public long getVersion()
    {
        return version;
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

    public ProcessDefinition getDefinition()
    {
        return definition;
    }

    public List<ProcessExecutionEntity> getExecutions()
    {
        return executions;
    }

    public void setMonitor(ProcessExecutionMonitorEntity monitor)
    {
        this.monitor = monitor;
    }
    
    public ProcessExecutionMonitorEntity getMonitor()
    {
        return monitor;
    }
    
    public ZonedDateTime getExecutedOn()
    {
        // Find the time when the latest execution started
        
        if (executions.isEmpty())
            return null;
        else if (executions.size() == 1)
            return executions.get(0).getStartedOn();
        
        // More than 1 executions are present: find the latest
        
        return executions.stream()
            .map(ProcessExecutionEntity::getStartedOn)
            .filter(Objects::nonNull)
            .sorted(Comparator.reverseOrder())
            .findFirst()
            .orElse(null);
    }
    
    public ProcessIdentifier getProcessIdentifier()
    {
        return ProcessIdentifier.of(parent.id, version);
    }
    
    public ProcessRecord toProcessRecord()
    {
        return toProcessRecord(false, false);
    }
    
    public ProcessRecord toProcessRecord(boolean includeExecutions, boolean includeSteps) 
    {
        ProcessRecord record = new ProcessRecord(parent.id, version);
        AccountEntity createdBy = parent.createdBy;

        record.setCreatedOn(parent.createdOn);
        record.setCreatedBy(createdBy.getId(), createdBy.getFullName());
        record.setUpdatedOn(updatedOn);
        record.setUpdatedBy(updatedBy.getId(), updatedBy.getFullName());
        record.setDescription(description);
        record.setName(name);
        record.setTaskType(parent.getTaskType());
        record.setDefinition(definition);
        record.setTemplate(parent.isTemplate);

        record.setExecutedOn(getExecutedOn());
        
        if (includeExecutions) {
            for (ProcessExecutionEntity e: executions) {
                record.addExecution(e.toProcessExecutionRecord(includeSteps));
            }
        }

        return record;
    }
}
