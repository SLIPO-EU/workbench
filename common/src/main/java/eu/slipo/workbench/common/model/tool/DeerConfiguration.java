package eu.slipo.workbench.common.model.tool;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.slipo.workbench.common.model.poi.EnumTool;

/**
 * Configuration for DEER
 */
public class DeerConfiguration extends EnrichConfiguration 
{
    private static final long serialVersionUID = 1L;

    public DeerConfiguration() 
    {
    }

    // TODO: Remove
    private String temp;

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    @JsonIgnore
    @Override
    public EnumTool getTool()
    {
        return EnumTool.DEER;
    }
}
