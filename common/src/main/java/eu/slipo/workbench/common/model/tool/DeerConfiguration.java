package eu.slipo.workbench.common.model.tool;

import java.util.List;

import org.apache.commons.lang3.NotImplementedException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.tool.output.InputToOutputNameMapper;

/**
 * Configuration for DEER
 */
public class DeerConfiguration extends EnrichConfiguration<Deer>
{
    private static final long serialVersionUID = 1L;

    // TODO: Temporary fix for serializing empty configuration. Should be removed once
    // configuration specifications are implemented
    @JsonProperty
    public String specs;

    public DeerConfiguration() {}

    @JsonIgnore
    @Override
    public Class<Deer> getToolType()
    {
        return Deer.class;
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
    public void setOutputDir(String dir)
    {
        super.setOutputDir(dir);
    }

    @JsonIgnore
    @Override
    public void setOutputFormat(EnumDataFormat dataFormat)
    {
        super.setOutputFormat(dataFormat);
    }

    @JsonIgnore
    @Override
    public EnumDataFormat getOutputFormat()
    {
        return super.getOutputFormat();
    }

    @JsonIgnore
    @Override
    public InputToOutputNameMapper<Deer> getOutputNameMapper()
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
