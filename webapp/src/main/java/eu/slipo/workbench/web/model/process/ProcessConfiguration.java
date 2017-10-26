package eu.slipo.workbench.web.model.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProcessConfiguration {

    private List<ProcessResource> resources = new ArrayList<ProcessResource>();

    private List<Step> steps = new ArrayList<Step>();

    private List<Integer> execution = new ArrayList<Integer>();

    public ProcessConfiguration() {

    }

    public ProcessConfiguration(List<ProcessResource> resources, List<Step> steps, List<Integer> execution) {
        this.resources = resources;
        this.steps = steps;
        this.execution = execution;
    }

    public List<ProcessResource> getResources() {
        return Collections.unmodifiableList(resources);
    }

    public List<Step> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    public List<Integer> getExecution() {
        return Collections.unmodifiableList(execution);
    }

}
