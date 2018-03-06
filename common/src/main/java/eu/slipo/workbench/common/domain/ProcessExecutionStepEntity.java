package eu.slipo.workbench.common.domain;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

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
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;

import eu.slipo.workbench.common.model.poi.EnumOperation;
import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.process.EnumProcessExecutionStatus;
import eu.slipo.workbench.common.model.process.ProcessExecutionStepRecord;


@Entity(name = "ProcessExecutionStep")
@Table(
    schema = "public", name = "process_execution_step",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_process_execution_step_execution_and_key", 
            columnNames = { "process_execution", "step_key" }),
    }
)
public class ProcessExecutionStepEntity 
{
    @Id
    @Column(name = "id", updatable = false)
    @SequenceGenerator(
        sequenceName = "process_execution_step_id_seq", name = "process_execution_step_id_seq", initialValue = 1, allocationSize = 1)
    @GeneratedValue(generator = "process_execution_step_id_seq", strategy = GenerationType.SEQUENCE)
    long id = -1L;

    @NotNull
    @NaturalId
    @ManyToOne
    @JoinColumn(name = "process_execution", nullable = false, updatable = false)
    ProcessExecutionEntity execution;

    @Min(0)
    @NotNull
    @NaturalId
    @Column(name = "step_key", nullable = false, updatable = false)
    int key;

    @NotNull
    @Column(name = "step_name", nullable = false, updatable = false)
    String name;
    
    @Min(1)
    @NotNull
    @Column(name = "job_execution", nullable = false, updatable = false)
    long jobExecutionId = -1L;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tool_name", nullable = false, updatable = false)
    EnumTool tool;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "operation", nullable = false, updatable = false)
    EnumOperation operation;

    @NotNull
    @Column(name = "started_on", nullable = false, updatable = false)
    ZonedDateTime startedOn;

    @Column(name = "completed_on")
    ZonedDateTime completedOn;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EnumProcessExecutionStatus status;

    @Column(name = "error_message", length = 2048)
    private String errorMessage;

    @OneToMany(mappedBy = "step", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<ProcessExecutionStepFileEntity> files = new ArrayList<>();

    protected ProcessExecutionStepEntity() {}
    
    public ProcessExecutionStepEntity(ProcessExecutionEntity executionEntity, int key, String name) 
    {
        this.execution = executionEntity;
        this.key = key;
        this.name = name;
    }
    
    public ProcessExecutionStepEntity(
        ProcessExecutionEntity executionEntity, int key, String name, long jobExecutionId) 
    {
        this.execution = executionEntity;
        this.key = key;
        this.name = name;
        this.jobExecutionId = jobExecutionId;
    }
    
    public long getId() 
    {
        return id;
    }

    public ProcessExecutionEntity getExecution() 
    {
        return execution;
    }

    public void setExecution(ProcessExecutionEntity execution) 
    {
        this.execution = execution;
    }

    public int getKey() 
    {
        return key;
    }

    public String getName() 
    {
        return name;
    }

    public void setName(String name) 
    {
        this.name = name;
    }

    public void setKey(int key) 
    {
        this.key = key;
    }

    public long getJobExecutionId()
    {
        return jobExecutionId;
    }

    public void setJobExecutionId(long jobExecutionId)
    {
        this.jobExecutionId = jobExecutionId;
    }

    public EnumTool getTool() 
    {
        return tool;
    }

    public void setTool(EnumTool tool) 
    {
        this.tool = tool;
    }

    public EnumOperation getOperation() 
    {
        return operation;
    }

    public void setOperation(EnumOperation operation) 
    {
        this.operation = operation;
    }

    public ZonedDateTime getStartedOn() 
    {
        return startedOn;
    }

    public void setStartedOn(ZonedDateTime startedOn) 
    {
        this.startedOn = startedOn;
    }

    public ZonedDateTime getCompletedOn() 
    {
        return completedOn;
    }

    public void setCompletedOn(ZonedDateTime completedOn) 
    {
        this.completedOn = completedOn;
    }

    public EnumProcessExecutionStatus getStatus() 
    {
        return status;
    }

    @AssertTrue
    protected boolean isStatusValid()
    {
        boolean check = true;
        switch (status) {
        case UNKNOWN:
            check = false;
            break;
        case RUNNING:
            check = completedOn == null;
            break;
        case COMPLETED:
        case FAILED:
            check = completedOn != null;
        default:
            break;
        }
        
        return check;
    }
    
    public void setStatus(EnumProcessExecutionStatus status) 
    {
        this.status = status;
    }

    public String getErrorMessage() 
    {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    public List<ProcessExecutionStepFileEntity> getFiles() 
    {
        return files;
    }

    public void addFile(ProcessExecutionStepFileEntity f) 
    {
        f.setStep(this);
        files.add(f);
    }
    
    public ProcessExecutionStepRecord toProcessExecutionStepRecord() 
    {
        ProcessExecutionStepRecord stepRecord = new ProcessExecutionStepRecord(id, key, name);

        stepRecord.setTool(tool);
        stepRecord.setOperation(operation);
        stepRecord.setStartedOn(startedOn);
        stepRecord.setJobExecutionId(jobExecutionId);
        stepRecord.setCompletedOn(completedOn);
        stepRecord.setErrorMessage(errorMessage);
        stepRecord.setStatus(status);
        
        for (ProcessExecutionStepFileEntity f: files) {
            stepRecord.addFile(f.toProcessExecutionStepFileRecord());
        }

        return stepRecord;
    }

}
