package eu.slipo.workbench.web.model.process;

public class ProcessCreateRequest {

    private EnumProcessSaveAction action;

    private ProcessDefinitionUpdate process;

    public EnumProcessSaveAction getAction() {
        return action;
    }

    public void setAction(EnumProcessSaveAction action) {
        this.action = action;
    }

    public ProcessDefinitionUpdate getProcess() {
        return process;
    }

    public void setProcess(ProcessDefinitionUpdate process) {
        this.process = process;
    }

}
