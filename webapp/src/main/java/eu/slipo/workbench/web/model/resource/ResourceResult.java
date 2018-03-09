package eu.slipo.workbench.web.model.resource;

import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.resource.ResourceRecord;

public class ResourceResult {

    private ResourceRecord resource;

    private ProcessExecutionRecord execution;

    public ResourceResult(ResourceRecord resource, ProcessExecutionRecord execution) {
        this.resource = resource;
        this.execution = execution;
    }

    public ResourceRecord getResource() {
        return resource;
    }

    public ProcessExecutionRecord getExecution() {
        return execution;
    }

}
