package eu.slipo.workbench.common.model.process;

public class ProcessViewResult {

    private ProcessDefinitionView process;

    public ProcessDefinitionView getProcess() {
        return process;
    }

    public ProcessViewResult(ProcessDefinitionView process) {
        this.process = process;
    }

    public void setProcess(ProcessDefinitionView process) {
        this.process = process;
    }

}
