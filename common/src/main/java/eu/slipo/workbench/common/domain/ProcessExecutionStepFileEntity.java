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
import javax.validation.constraints.NotNull;

import eu.slipo.workbench.common.model.process.EnumStepFile;
import eu.slipo.workbench.common.model.process.ProcessExecutionStepFileRecord;


@Entity(name = "ProcessExecutionStepFile")
@Table(schema = "public", name = "process_execution_step_file")
public class ProcessExecutionStepFileEntity {

    @Id
    @Column(name = "id")
    @SequenceGenerator(
        sequenceName = "process_execution_step_file_id_seq", name = "process_execution_step_file_id_seq", initialValue = 1, allocationSize = 1)
    @GeneratedValue(generator = "process_execution_step_file_id_seq", strategy = GenerationType.SEQUENCE)
    long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "process_execution_step", nullable = false)
    ProcessExecutionStepEntity step;

    @ManyToOne()
    @JoinColumn(name = "resource", nullable = true)
    ResourceRevisionEntity resource;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "`type`")
    EnumStepFile type;

    @Column(name = "file_path")
    String path;

    @Column(name = "file_size")
    Long size;

    @Column(name = "table_name")
    UUID tableName;

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

    public String getFileName() {
        return path;
    }

    public void setFileName(String path) {
        this.path = path;
    }

    public Long getFileSize() {
        return size;
    }

    public void setSize(long fileSize) {
        this.size = fileSize;
    }

    public UUID getTableName() {
        return tableName;
    }

    public void setTableName(UUID tableName) {
        this.tableName = tableName;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ProcessExecutionStepFileRecord toProcessExecutionStepFileRecord() {
        ProcessExecutionStepFileRecord f = new ProcessExecutionStepFileRecord();

        f.setId(this.id);
        f.setType(this.type);
        f.setFilePath(this.path);
        f.setFileSize(this.size);
        if (this.resource != null) {
            f.setResource(this.resource.parent.id, this.resource.version);
        }
        f.setTableName(this.tableName);

        return f;
    }

}
