package eu.slipo.workbench.common.domain;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity(name = "Workflow")
@Table(schema = "public", name = "workflow")
public class WorkflowEntity
{
    @Id
    @Column(name = "id", updatable = false, columnDefinition = "uuid")
    UUID id;
    
    @NotNull
    @OneToOne
    @JoinColumn(name = "process", nullable = false, unique = true, updatable = false)
    ProcessRevisionEntity process;
    
    public WorkflowEntity() {}
    
    public WorkflowEntity(UUID workflowId, ProcessRevisionEntity processRevisionEntity) 
    {
        this.id = workflowId;
        this.process = processRevisionEntity;
    }
    
    public UUID getId()
    {
        return id;
    }
    
    public ProcessRevisionEntity getProcess()
    {
        return process;
    }
}
