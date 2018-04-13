package eu.slipo.workbench.common.model.tool;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.slipo.workbench.common.model.poi.EnumTool;

/**
 * Configuration for FAGI
 */
public class FagiConfiguration extends FuseConfiguration 
{
    private static final long serialVersionUID = 1L;

    public FagiConfiguration() 
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
        return EnumTool.FAGI;
    }
}
