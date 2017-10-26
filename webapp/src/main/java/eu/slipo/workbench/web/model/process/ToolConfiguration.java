package eu.slipo.workbench.web.model.process;

import java.util.ArrayList;
import java.util.List;

import eu.slipo.workbench.web.model.EnumTool;

public abstract class ToolConfiguration {

    protected List<Integer> resources = new ArrayList<Integer>();

    protected EnumTool tool;

    protected ToolConfiguration() {

    }

    public EnumTool getTool() {
        return tool;
    }

    public List<Integer> getResources() {
        return resources;
    }

}
