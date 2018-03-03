package eu.slipo.workbench.common.domain;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

@Entity(name = "ProcessExecutionMonitor")
@Table(schema = "public", name = "process_execution_monitor")
public class ProcessExecutionMonitorEntity
{    
    @Id
    long id;
    
    @NotNull
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "process", updatable = false, nullable = false)
    @MapsId
    ProcessRevisionEntity process;
    
    @NotNull
    @Column(name = "modified_on", nullable = false)
    ZonedDateTime modifiedOn;
    
    @Version
    @Column(name = "row_version")
    long rowVersion;
    
    protected ProcessExecutionMonitorEntity() {}
    
    public ProcessExecutionMonitorEntity(
        ProcessRevisionEntity processRevisionEntity, ZonedDateTime modifiedOn)
    {
        this.process = processRevisionEntity;
        this.modifiedOn = modifiedOn;
    }
    
    public ProcessRevisionEntity getProcess()
    {
        return process;
    }
    
    public ZonedDateTime getModifiedOn()
    {
        return modifiedOn;
    }
    
    public void setModifiedOn(ZonedDateTime modifiedOn)
    {
        this.modifiedOn = modifiedOn;
    }
}
