package eu.slipo.workbench.web.model.resource;

import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.common.model.resource.ResourceRecord;

public class MapDataResult {

    private ResourceRecord resource;

    private ProcessRecord process;

    private ProcessExecutionRecord execution;

    private Long version;

    public MapDataResult(ResourceRecord resource, ProcessRecord process, ProcessExecutionRecord execution, long version) {
        this.resource = resource;
        this.process = process;
        this.execution = execution;
        this.version = version;
    }

    public MapDataResult(ProcessRecord process, ProcessExecutionRecord execution) {
        this.process = process;
        this.execution = execution;
    }

    public ResourceRecord getResource() {
        return resource;
    }

    public ProcessRecord getProcess() {
        return process;
    }

    public ProcessExecutionRecord getExecution() {
        return execution;
    }

    public Long getVersion() {
        return version;
    }

}
