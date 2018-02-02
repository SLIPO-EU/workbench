package eu.slipo.workbench.web.domain;

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

import eu.slipo.workbench.web.model.EnumOperation;
import eu.slipo.workbench.web.model.EnumTool;
import eu.slipo.workbench.web.model.process.EnumProcessExecutionStatus;
import eu.slipo.workbench.web.model.process.ProcessExecutionStepRecord;

@Entity(name = "ProcessExecutionStep")
@Table(schema = "public", name = "process_execution_step")
public class ProcessExecutionStepEntity {

    @Id
    @Column(name = "id")
    @SequenceGenerator(sequenceName = "process_execution_step_id_seq", name = "process_execution_step_id_seq", initialValue = 1, allocationSize = 1)
    @GeneratedValue(generator = "process_execution_step_id_seq", strategy = GenerationType.SEQUENCE)
    long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "process_execution", nullable = false)
    ProcessExecutionEntity execution;

    @NotNull()
    @Column(name = "step_key")
    int key;

    @NotNull
    @Column(name = "step_name")
    String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tool_name")
    EnumTool component;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "operation")
    EnumOperation operation;

    @NotNull
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

    @OneToMany(mappedBy = "step", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<ProcessExecutionStepFileEntity> files = new ArrayList<>();

    public long getId() {
        return id;
    }

    public ProcessExecutionEntity getExecution() {
        return execution;
    }

    public void setExecution(ProcessExecutionEntity execution) {
        this.execution = execution;
    }

    public int getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public EnumTool getComponent() {
        return component;
    }

    public void setComponent(EnumTool component) {
        this.component = component;
    }

    public EnumOperation getOperation() {
        return operation;
    }

    public void setOperation(EnumOperation operation) {
        this.operation = operation;
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

    public List<ProcessExecutionStepFileEntity> getFiles() {
        return files;
    }

    public ProcessExecutionStepRecord toProcessExecutionStepRecord() {
        ProcessExecutionStepRecord s = new ProcessExecutionStepRecord(this.id, this.key, this.name);

        s.setComponent(this.component);
        s.setOperation(this.operation);
        s.setStartedOn(this.startedOn);
        s.setCompletedOn(this.completedOn);
        s.setErrorMessage(this.errorMessage);
        s.setStatus(this.status);

        for (ProcessExecutionStepFileEntity f : this.getFiles()) {
            s.addFile(f.toProcessExecutionStepFileRecord());
        }

        return s;
    }

}
