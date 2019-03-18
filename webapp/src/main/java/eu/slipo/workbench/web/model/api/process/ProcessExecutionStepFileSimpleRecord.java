package eu.slipo.workbench.web.model.api.process;

import java.nio.file.Paths;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vividsolutions.jts.geom.Geometry;

import eu.slipo.workbench.common.model.process.EnumStepFile;
import eu.slipo.workbench.common.model.process.ProcessExecutionStepFileRecord;

public class ProcessExecutionStepFileSimpleRecord {

    private long id;
    private EnumStepFile type;
    private String name;
    private Long size;
    private Geometry boundingBox;

    public ProcessExecutionStepFileSimpleRecord(ProcessExecutionStepFileRecord record) {
        this.id = record.getId();
        this.type = record.getType();
        this.name = Paths.get(record.getFilePath()).getFileName().toString();
        this.size = record.getFileSize();
        this.boundingBox = record.getBoundingBox();
    }

    public long getId() {
        return id;
    }

    public EnumStepFile getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Long getSize() {
        return size;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Geometry getBoundingBox() {
        return boundingBox;
    }

}
