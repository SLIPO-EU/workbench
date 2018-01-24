package eu.slipo.workbench.web.model.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Process designer model for create/update operations
 */
public class ProcessDefinitionUpdate {

    private Long id;

    private String name;

    private String description;

    private List<ProcessInput> resources = new ArrayList<ProcessInput>();

    private List<Step> steps = new ArrayList<Step>();

    protected ProcessDefinitionUpdate() {

    }

    public ProcessDefinitionUpdate(Long id, List<ProcessInput> resources, List<Step> steps) {
        this.id = id;
        this.resources = resources;
        this.steps = steps;
    }

    public ProcessDefinitionUpdate(List<ProcessInput> resources, List<Step> steps) {
        this.resources = resources;
        this.steps = steps;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Input resources
     *
     * @return a list of the input resources
     */
    public List<ProcessInput> getResources() {
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

}
