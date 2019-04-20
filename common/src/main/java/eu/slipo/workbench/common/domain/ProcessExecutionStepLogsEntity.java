package eu.slipo.workbench.common.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;

import eu.slipo.workbench.common.model.process.ProcessExecutionStepLogsRecord;

@Entity(name = "ProcessExecutionStepLogs")
@Table(schema = "public", name = "process_execution_step_logs")
public class ProcessExecutionStepLogsEntity
{
    @Id
    @Column(name = "id", updatable = false)
    @SequenceGenerator(
        sequenceName = "process_execution_step_logs_id_seq", name = "process_execution_step_logs_id_seq", initialValue = 1, allocationSize = 1)
    @GeneratedValue(generator = "process_execution_step_logs_id_seq", strategy = GenerationType.SEQUENCE)
    long id = -1L;

    @NotNull
    @NaturalId
    @ManyToOne
    @JoinColumn(name = "process_execution_step", nullable = false, updatable = false)
    ProcessExecutionStepEntity step;
    
    /**
     * The name of the underlying Batch step (this is <em>not</em> the name of the process execution step!)
     */
    @NotNull
    @NaturalId
    @Column(name = "`name`", nullable = false, updatable = false)
    String name;
    
    @NotNull
    @Column(name = "file_path", nullable = false, updatable = false)
    String path;
    
    protected ProcessExecutionStepLogsEntity() {}
    
    public ProcessExecutionStepLogsEntity(ProcessExecutionStepEntity processExecutionStepEntity, String name)
    {
        this(processExecutionStepEntity, name, null);
    }
    
    public ProcessExecutionStepLogsEntity(
        ProcessExecutionStepEntity processExecutionStepEntity, String name, String path)
    {
        this.step = processExecutionStepEntity;
        this.name = name;
        this.path = path;
    }
    
    public ProcessExecutionStepEntity getStep()
    {
        return step;
    }
    
    public String getName()
    {
        return name;
    }
    
    public String getPath()
    {
        return path;
    }
    
    public void setPath(String path)
    {
        this.path = path;
    }

    public ProcessExecutionStepLogsRecord toProcessExecutionStepLogsRecord()
    {
        ProcessExecutionStepLogsRecord r = new ProcessExecutionStepLogsRecord(name, path);
        return r;
    }
}
