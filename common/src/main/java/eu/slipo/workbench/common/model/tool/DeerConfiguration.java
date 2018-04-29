package eu.slipo.workbench.common.model.tool;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumOutputType;
import eu.slipo.workbench.common.model.poi.EnumTool;

/**
 * Configuration for DEER
 */
public class DeerConfiguration extends EnrichConfiguration 
{
    private static final long serialVersionUID = 1L;

    public DeerConfiguration() {}

    @JsonIgnore
    @Override
    public EnumTool getTool()
    {
        return EnumTool.DEER;
    }

    @JsonIgnore
    @Override
    public EnumDataFormat getInputFormat()
    {
        return super.getInputFormat();
    }

    @JsonIgnore
    @Override
    public void setInputFormat(EnumDataFormat inputFormat)
    {
        super.setInputFormat(inputFormat);
    }

    @JsonIgnore
    @Override
    public List<String> getInput()
    {
        return super.getInput();
    }

    @JsonIgnore
    @Override
    public void setInput(List<String> input)
    {
        super.setInput(input);
    }

    @JsonIgnore
    @Override
    public void setInput(String input)
    {
        super.setInput(input);
    }

    @JsonIgnore
    @Override
    public void clearInput()
    {
        super.clearInput();
    }

    @JsonIgnore
    @Override
    public String getOutputDir()
    {
        return super.getOutputDir();
    }

    @JsonIgnore
    @Override
    public EnumDataFormat getOutputFormat()
    {
        return super.getOutputFormat();
    }

    @JsonIgnore
    @Override
    public Map<EnumOutputType, List<String>> getOutputNames()
    {
        throw new NotImplementedException("not implemented yet");
    }
    
    @JsonIgnore
    @Override
    public String getVersion()
    {
        return super.getVersion();
    }

    @JsonIgnore
    @Override
    public void setVersion(String version)
    {
        super.setVersion(version);
    }    
}
