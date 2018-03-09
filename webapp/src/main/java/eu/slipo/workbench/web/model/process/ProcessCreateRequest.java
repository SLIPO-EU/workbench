package eu.slipo.workbench.web.model.process;

import eu.slipo.workbench.common.model.process.ProcessDefinition;

public class ProcessCreateRequest {

    private EnumProcessSaveActionType action;

    private ProcessDefinition definition;

    public EnumProcessSaveActionType getAction() {
        return action;
    }

    public void setAction(EnumProcessSaveActionType action) {
        this.action = action;
    }

    public ProcessDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(ProcessDefinition process) {
        this.definition = process;
    }

}
