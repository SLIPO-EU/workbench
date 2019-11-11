package eu.slipo.workbench.web.model.process;

import java.io.Serializable;
import java.time.ZonedDateTime;

import eu.slipo.workbench.common.model.process.EnumProcessTaskType;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessDraftRecord;
import eu.slipo.workbench.common.model.process.ProcessRecord;

public class ProcessRecordView implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Long id;

    private final Long version;

    private final boolean template;

    private final EnumProcessTaskType taskType;

    private final ProcessDefinition definition;

    private final ZonedDateTime modified;

    private final ProcessDraftRecord draft;

    public ProcessRecordView(ProcessRecord processRecord, ProcessDraftRecord draft) {
        this.id = processRecord.getId();
        this.version = processRecord.getVersion();
        this.template = processRecord.isTemplate();
        this.definition = processRecord.getDefinition();
        this.taskType = processRecord.getTaskType();
        this.modified = processRecord.getUpdatedOn();

        // Set draft property only if it is updated after the process was saved
        if (draft != null && draft.getUpdatedOn().isAfter(processRecord.getUpdatedOn())) {
            this.draft = draft;
        } else {
            this.draft = null;
        }
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

    public EnumProcessTaskType getTaskType() {
        return taskType;
    }

    public ZonedDateTime getModified() {
        return modified;
    }

    public ProcessDraftRecord getDraft() {
        return draft;
    }

}
