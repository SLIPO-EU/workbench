package eu.slipo.workbench.web.model.process;

import eu.slipo.workbench.common.model.process.ProcessDefinition;

public class ProcessCreateRequest {

    private EnumProcessSaveActionType action;

    private ProcessDefinition process;

    public EnumProcessSaveActionType getAction() {
        return action;
    }

    public void setAction(EnumProcessSaveActionType action) {
        this.action = action;
    }

    public ProcessDefinition getProcess() {
        return process;
    }

    public void setProcess(ProcessDefinition process) {
        this.process = process;
    }

}
