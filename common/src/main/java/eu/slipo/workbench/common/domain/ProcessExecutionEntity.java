package eu.slipo.workbench.common.domain;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
import javax.persistence.OneToOne;
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
    @Column(name = "status", nullable = false)
    private EnumProcessExecutionStatus status;

    @Column(name = "error_message", length = 2047)
    private String errorMessage;

    @OneToMany(
        mappedBy = "execution",
        fetch = FetchType.LAZY,
        cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH },
        orphanRemoval = false)
    List<ProcessExecutionStepEntity> steps = new ArrayList<>();

    @OneToOne(
        mappedBy = "workflow",
        fetch = FetchType.EAGER,
        cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH })
    ProcessExecutionMapExportEntity map;

    public static final Comparator<ProcessExecutionEntity> ORDER_BY_STARTED =
        Comparator.comparing(e -> e.getStartedOn());

    public static final Comparator<ProcessExecutionEntity> ORDER_BY_SUBMITTED =
        Comparator.comparing(e -> e.getSubmittedOn());

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
        return status.isRunning();
    }

    public boolean isFinished()
    {
        return status.isFinished();
    }

    public boolean isTerminated()
    {
        return status.isTerminated();
    }

    public boolean isTerminated(boolean deep)
    {
        // An execution may report as terminated although some steps may still report as
        // running. This is because workflow listeners that actually mark a step/execution
        // are not synchronized (and do not have to be). The overall status will eventually
        // be consistent, so usually this is not a problem.

        // If you want to be sure that an execution has settled down (i.e execution and steps
        // report as terminated) you can use deep=true to check both own status and all statuses
        // from steps.

        if (!deep) {
            return status.isTerminated();
        } else {
            return status.isTerminated() &&
                steps.stream().allMatch(step -> step.getStatus().isTerminated());
        }
    }

    @AssertTrue
    protected boolean isStatusValid()
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
            if (step.key == stepKey) {
                return step;
            }
        }
        return null;
    }

    public ProcessExecutionRecord toProcessExecutionRecord()
    {
        return toProcessExecutionRecord(true, false);
    }

    public ProcessExecutionRecord toProcessExecutionRecord(boolean includeSteps)
    {
        return toProcessExecutionRecord(includeSteps, false);
    }

    public ProcessExecutionMapExportEntity getMap()
    {
        return map;
    }

    public void setMap(ProcessExecutionMapExportEntity map)
    {
        this.map = map;
    }

    public ProcessExecutionRecord toProcessExecutionRecord(boolean includeSteps, boolean includeNonVerifiedFiles)
    {
        ProcessExecutionRecord record =
            new ProcessExecutionRecord(id, process.parent.id, process.version);

        if (submittedBy != null) {
            record.setSubmittedBy(submittedBy.getId(), submittedBy.getFullName());
        }
        record.setSubmittedOn(submittedOn);
        record.setStartedOn(startedOn);
        record.setCompletedOn(completedOn);
        record.setStatus(status);
        record.setTaskType(process.getParent().getTaskType());
        record.setName(process.getName());
        record.setErrorMessage(errorMessage);
        record.setRunning(!isTerminated(true));

        if (this.map != null) {
            record.setExportedBy(this.map.getCreatedBy().getId(), this.map.getCreatedBy().getFullName());
            record.setExportedOn(this.map.getCreatedOn());
            record.setExportStatus(this.map.getStatus());
        }

        if (includeSteps) {
            for (ProcessExecutionStepEntity s: steps) {
                record.addStep(s.toProcessExecutionStepRecord(includeNonVerifiedFiles));
            }
        }

        return record;
    }
}
