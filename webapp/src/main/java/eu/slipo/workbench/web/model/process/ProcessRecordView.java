package eu.slipo.workbench.web.model.process;

import java.io.Serializable;

import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessRecord;

public class ProcessRecordView implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Long id;

    private final Long version;

    private final boolean template;

    private final ProcessDefinition definition;

    public ProcessRecordView(ProcessRecord processRecord) {
        this.id = processRecord.getId();
        this.version = processRecord.getVersion();
        this.template = processRecord.isTemplate();
        this.definition = processRecord.getDefinition();
    }

    public Long getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public boolean isTemplate() {
        return template;
    }

    public ProcessDefinition getDefinition() {
        return definition;
    }

}
