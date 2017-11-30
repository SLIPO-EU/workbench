package eu.slipo.workbench.web.model.process;

import java.util.ArrayList;
import java.util.List;

import eu.slipo.workbench.web.model.EnumTool;

/**
 * Generic configuration for SLIPO Toolkit components
 */
public abstract class ToolConfiguration {

    protected EnumTool tool;

    protected List<Integer> resources = new ArrayList<Integer>();

    protected ToolConfiguration() {

    }

    protected ToolConfiguration(EnumTool tool) {
        this.tool = tool;
    }

    public EnumTool getTool() {
        return tool;
    }

    /**
     * Indexes of the input resources
     *
     * @return the index values
     */
    public List<Integer> getResources() {
        return resources;
    }

}
