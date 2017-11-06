package eu.slipo.workbench.web.model.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Process designer model for create and update operations
 */
public class ProcessDesignerUpdateModel {

    private List<ProcessResource> resources = new ArrayList<ProcessResource>();

    private List<Step> steps = new ArrayList<Step>();

    private List<Integer> execution = new ArrayList<Integer>();

    protected ProcessDesignerUpdateModel() {

    }

    public ProcessDesignerUpdateModel(List<ProcessResource> resources, List<Step> steps, List<Integer> execution) {
        this.resources = resources;
        this.steps = steps;
        this.execution = execution;
    }

    /**
     * Input resources
     *
     * @return a list of the input resources
     */
    public List<ProcessResource> getResources() {
        return Collections.unmodifiableList(resources);
    }

    /**
     * Process steps
     *
     * @return a list of all process steps
     */
    public List<Step> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    /**
     * Order of step execution
     *
     * @return a list of step indexes
     */
    public List<Integer> getExecution() {
        return Collections.unmodifiableList(execution);
    }

}
