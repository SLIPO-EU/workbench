package eu.slipo.workbench.common.model.process;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProcessDefinition implements Serializable 
{
    private static final long serialVersionUID = 1L;

    private String name;

    private String description;

    private List<ProcessInput> resources = new ArrayList<ProcessInput>();

    private List<Step> steps = new ArrayList<Step>();

    protected ProcessDefinition() {}

    public ProcessDefinition(String name, List<ProcessInput> resources, List<Step> steps) 
    {
        this.name = name;
        this.resources = resources;
        this.steps = steps;
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
    public List<ProcessInput> getResources() 
    {
        return Collections.unmodifiableList(resources);
    }

    /**
     * Process steps
     *
     * @return a list of all process steps
     */
    public List<Step> getSteps() 
    {
        return Collections.unmodifiableList(steps);
    }

}
