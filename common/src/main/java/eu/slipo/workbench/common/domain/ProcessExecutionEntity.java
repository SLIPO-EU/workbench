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
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import eu.slipo.workbench.common.model.process.EnumProcessExecutionStatus;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;

@Entity(name = "ProcessExecution")
@Table(schema = "public", name = "process_execution")
public class ProcessExecutionEntity 
{
    @Id
    @Column(name = "id", updatable = false)
    @SequenceGenerator(
        sequenceName = "process_execution_id_seq", 
        name = "process_execution_id_seq", 
        initialValue = 1, 
        allocationSize = 1)
    @GeneratedValue(generator = "process_execution_id_seq", strategy = GenerationType.SEQUENCE)
    long id = -1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by", updatable = false)
    AccountEntity submittedBy;

    @NotNull
    @Column(name = "submitted_on", nullable = false, updatable = false)
    ZonedDateTime submittedOn;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "process", nullable = false, updatable = false)
    ProcessRevisionEntity process;

    @Column(name = "started_on")
    ZonedDateTime startedOn;

    @Column(name = "completed_on")
    ZonedDateTime completedOn;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EnumProcessExecutionStatus status;

    @Column(name = "error_message")
    private String errorMessage;

    @OneToMany(
        mappedBy = "execution", 
        fetch = FetchType.LAZY, 
        cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH }, 
        orphanRemoval = false)
    List<ProcessExecutionStepEntity> steps = new ArrayList<>();

    protected ProcessExecutionEntity() {}
    
    public ProcessExecutionEntity(ProcessRevisionEntity processRevisionEntity)
    {
        this.process = processRevisionEntity;
        this.status = EnumProcessExecutionStatus.UNKNOWN;
    }
    
    public long getId() 
    {
        return id;
    }

    public void setId(long id) 
    {
        this.id = id;
    }

    public ProcessRevisionEntity getProcess() 
    {
        return process;
    }

    public void setProcess(ProcessRevisionEntity process) 
    {
        this.process = process;
    }

    public AccountEntity getSubmittedBy() 
    {
        return submittedBy;
    }

    public void setSubmittedBy(AccountEntity submittedBy) 
    {
        this.submittedBy = submittedBy;
    }

    public ZonedDateTime getSubmittedOn() 
    {
        return submittedOn;
    }

    public void setSubmittedOn(ZonedDateTime submittedOn) 
    {
        this.submittedOn = submittedOn;
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

    public void setStatus(EnumProcessExecutionStatus status) 
    {
        this.status = status;
    }
    
    public boolean isRunning()
    {
        return status == EnumProcessExecutionStatus.RUNNING;
    }
    
    public boolean isFinished()
    {
        return status == EnumProcessExecutionStatus.COMPLETED || 
            status == EnumProcessExecutionStatus.FAILED;
    }
    
    public boolean isTerminated()
    {
        return status == EnumProcessExecutionStatus.COMPLETED || 
            status == EnumProcessExecutionStatus.FAILED ||
            status == EnumProcessExecutionStatus.STOPPED;
    }
    
    @AssertTrue
    public boolean isStatusValid()
    {
        boolean check = true;
        switch (status) {
        case UNKNOWN:
            check = startedOn == null && completedOn == null;
            break;
        case RUNNING:
            check = completedOn == null;
            break;
        case COMPLETED:
        case FAILED:
            check = completedOn != null;
            break;
        default:
            break;
        }
        return check;
    }

    public String getErrorMessage() 
    {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) 
    {
        this.errorMessage = errorMessage;
    }

    public List<ProcessExecutionStepEntity> getSteps()
    {
        return steps;
    }

    public void addStep(ProcessExecutionStepEntity processExecutionStepEntity) 
    {    
        processExecutionStepEntity.setExecution(this);
        steps.add(processExecutionStepEntity);
    }

    public ProcessExecutionStepEntity getStepByKey(int stepKey)
    {
        for (ProcessExecutionStepEntity step: steps) {
            if (step.key == stepKey)
                return step;
        }
        return null;
    }
    
    public ProcessExecutionRecord toProcessExecutionRecord()
    {
        return toProcessExecutionRecord(true);
    }
    
    public ProcessExecutionRecord toProcessExecutionRecord(boolean includeSteps) 
    {
        ProcessExecutionRecord e = 
            new ProcessExecutionRecord(id, process.parent.id, process.version);

        if (submittedBy != null) {
            e.setSubmittedBy(submittedBy.getId(), submittedBy.getFullName());
        }
        e.setSubmittedOn(submittedOn);
        e.setStartedOn(startedOn);
        e.setCompletedOn(completedOn);
        e.setStatus(status);
        e.setName(process.getName());
        e.setErrorMessage(errorMessage);

        if (includeSteps) {
            for (ProcessExecutionStepEntity s: this.getSteps()) {
                e.addStep(s.toProcessExecutionStepRecord());
            }
        }

        return e;
    }
}
