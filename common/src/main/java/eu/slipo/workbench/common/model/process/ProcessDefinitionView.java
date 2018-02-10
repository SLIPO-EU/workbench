package eu.slipo.workbench.common.model.process;

public class ProcessDefinitionView extends ProcessDefinition {

    private Long version;

    protected ProcessDefinitionView() {
        super();
    }

    public ProcessDefinitionView(ProcessRecord process) 
    {
        super(
            process.getId(),
            process.getConfiguration().getResources(),
            process.getConfiguration().getSteps());

        this.version = process.getVersion();
        this.setName(process.getName());
        this.setDescription(process.getDescription());
    }

    public Long getVersion() {
        return version;
    }

}
