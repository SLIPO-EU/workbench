package eu.slipo.workbench.common.model.tool;

import java.io.Serializable;
import java.util.Properties;

import eu.slipo.workbench.common.model.poi.EnumTool;

public interface ToolConfiguration extends Serializable
{
    /**
     * Get the tool this configuration is intended for
     * 
     * @return the tool constant representing a tool
     */
    EnumTool getTool();
}
