package eu.slipo.workbench.common.domain;

import java.util.UUID;

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
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;

import eu.slipo.workbench.common.model.process.EnumStepFile;
import eu.slipo.workbench.common.model.process.ProcessExecutionStepFileRecord;


@Entity(name = "ProcessExecutionStepFile")
@Table(
    schema = "public", name = "process_execution_step_file", 
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_process_execution_step_file_step_and_path", 
            columnNames = { "process_execution_step", "file_path" }),
    })
public class ProcessExecutionStepFileEntity {

    @Id
    @Column(name = "id", updatable = false)
    @SequenceGenerator(
        sequenceName = "process_execution_step_file_id_seq", name = "process_execution_step_file_id_seq", initialValue = 1, allocationSize = 1)
    @GeneratedValue(generator = "process_execution_step_file_id_seq", strategy = GenerationType.SEQUENCE)
    long id = -1L;

    @NotNull
    @NaturalId
    @ManyToOne
    @JoinColumn(name = "process_execution_step", nullable = false, updatable = false)
    ProcessExecutionStepEntity step;

    @ManyToOne
    @JoinColumn(name = "resource", nullable = true)
    ResourceRevisionEntity resource;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "`type`", nullable = false, updatable = false)
    EnumStepFile type;

    @NotNull
    @NaturalId
    @Column(name = "file_path", nullable = false, updatable = false)
    String path;

    @Column(name = "file_size", updatable = false)
    Long size;

    protected ProcessExecutionStepFileEntity() {}
    
    public ProcessExecutionStepFileEntity(
        ProcessExecutionStepEntity stepExecutionEntity, EnumStepFile type, String filePath, Long fileSize)
    {
        this.step = stepExecutionEntity;
        this.path = filePath;
        this.type = type;
        this.size = fileSize;
    }
    
    public ProcessExecutionStepFileEntity(
        ProcessExecutionStepEntity stepExecutionEntity, EnumStepFile type, String filePath)
    {
        this(stepExecutionEntity, type, filePath, null);
    }
    
    public ProcessExecutionStepEntity getStep() {
        return step;
    }

    public void setStep(ProcessExecutionStepEntity step) {
        this.step = step;
    }

    public ResourceRevisionEntity getResource() {
        return resource;
    }

    public void setResource(ResourceRevisionEntity resource) {
        this.resource = resource;
    }

    public EnumStepFile getType() {
        return type;
    }

    public void setType(EnumStepFile type) {
        this.type = type;
    }

    public String getFilePath() {
        return path;
    }

    public void setFilePath(String path) {
        this.path = path;
    }

    public Long getFileSize() {
        return size;
    }

    public void setFileSize(long fileSize) {
        this.size = fileSize;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ProcessExecutionStepFileRecord toProcessExecutionStepFileRecord() 
    {
        ProcessExecutionStepFileRecord fileRecord = 
            new ProcessExecutionStepFileRecord(type, path, size);

        fileRecord.setId(id);
        
        if (resource != null)
            fileRecord.setResource(resource.parent.id, resource.version);

        return fileRecord;
    }

}
