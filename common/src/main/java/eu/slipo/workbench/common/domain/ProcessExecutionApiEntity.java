package eu.slipo.workbench.common.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import eu.slipo.workbench.common.model.poi.EnumOperation;
import eu.slipo.workbench.common.model.process.ProcessExecutionApiRecord;


@Entity(name = "ProcessExecutionApi")
@Table(
    schema = "public", name = "process_execution_api"
)
public class ProcessExecutionApiEntity
{
    @Id
    @Column(name = "id", updatable = false)
    @SequenceGenerator(
        sequenceName = "process_execution_api_seq", name = "process_execution_api_seq", initialValue = 1, allocationSize = 1
    )
    @GeneratedValue(generator = "process_execution_api_seq", strategy = GenerationType.SEQUENCE)
    private long id = -1L;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "process_execution", nullable = false, updatable = false)
    private ProcessExecutionEntity execution;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "application_key", nullable = false, updatable = false)
    private ApplicationKeyEntity key;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "operation", nullable = false)
    private EnumOperation operation;

    public ProcessExecutionEntity getExecution() {
        return execution;
    }

    public void setExecution(ProcessExecutionEntity execution) {
        this.execution = execution;
    }

    public ApplicationKeyEntity getKey() {
        return key;
    }

    public void setKey(ApplicationKeyEntity key) {
        this.key = key;
    }

    public EnumOperation getOperation() {
        return operation;
    }

    public void setOperation(EnumOperation operation) {
        this.operation = operation;
    }

    public long getId() {
        return id;
    }

    public ProcessExecutionApiRecord toRecord() {
        ProcessExecutionApiRecord record = new ProcessExecutionApiRecord();

        record.setExecution(execution.toProcessExecutionRecord());
        record.setId(id);
        record.setKey(key.toRecord());
        record.setOperation(operation);

        return record;
    }

}

