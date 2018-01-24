package eu.slipo.workbench.web.model.process;

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
