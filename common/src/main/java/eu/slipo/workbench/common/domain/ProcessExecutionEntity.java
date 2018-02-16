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
import javax.validation.constraints.NotNull;

import eu.slipo.workbench.common.model.process.EnumProcessExecutionStatus;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;

@Entity(name = "ProcessExecution")
@Table(schema = "public", name = "process_execution")
public class ProcessExecutionEntity 
{
    @Id
    @Column(name = "id")
    @SequenceGenerator(
        sequenceName = "process_execution_id_seq", name = "process_execution_id_seq", initialValue = 1, allocationSize = 1)
    @GeneratedValue(generator = "process_execution_id_seq", strategy = GenerationType.SEQUENCE)
    long id = -1L;

    @ManyToOne
    @JoinColumn(name = "submitted_by", nullable = true)
    AccountEntity submittedBy;

    @NotNull
    @Column(name = "submitted_on")
    ZonedDateTime submittedOn;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "process", nullable = false)
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
        mappedBy = "execution", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<ProcessExecutionStepEntity> steps = new ArrayList<>();

    private ProcessExecutionEntity() {}
    
    public ProcessExecutionEntity(ProcessRevisionEntity processRevisionEntity)
    {
        this.process = processRevisionEntity;
        this.status = EnumProcessExecutionStatus.UNKNOWN;
    }
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ProcessRevisionEntity getProcess() {
        return process;
    }

    public void setProcess(ProcessRevisionEntity process) {
        this.process = process;
    }

    public AccountEntity getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(AccountEntity submittedBy) {
        this.submittedBy = submittedBy;
    }

    public ZonedDateTime getSubmittedOn() {
        return submittedOn;
    }

    public void setSubmittedOn(ZonedDateTime submittedOn) {
        this.submittedOn = submittedOn;
    }

    public ZonedDateTime getStartedOn() {
        return startedOn;
    }

    public void setStartedOn(ZonedDateTime startedOn) {
        this.startedOn = startedOn;
    }

    public ZonedDateTime getCompletedOn() {
        return completedOn;
    }

    public void setCompletedOn(ZonedDateTime completedOn) {
        this.completedOn = completedOn;
    }

    public EnumProcessExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(EnumProcessExecutionStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
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

    public ProcessExecutionRecord toProcessExecutionRecord()
    {
        return toProcessExecutionRecord(false);
    }
    
    public ProcessExecutionRecord toProcessExecutionRecord(boolean includeSteps) 
    {
        ProcessExecutionRecord e = 
            new ProcessExecutionRecord(this.id, this.process.parent.id, this.process.version);

        if (this.submittedBy != null) {
            e.setSubmittedBy(this.submittedBy.getId(), this.submittedBy.getFullName());
        }
        e.setSubmittedOn(this.submittedOn);
        e.setStartedOn(this.startedOn);
        e.setCompletedOn(this.completedOn);
        e.setStatus(this.status);
        e.setName(this.getProcess().getName());
        e.setErrorMessage(this.errorMessage);

        if (includeSteps) {
            for (ProcessExecutionStepEntity s : this.getSteps()) {
                e.addStep(s.toProcessExecutionStepRecord());
            }
        }

        return e;
    }

}
