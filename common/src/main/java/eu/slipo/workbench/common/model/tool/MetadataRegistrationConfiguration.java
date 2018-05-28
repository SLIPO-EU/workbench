package eu.slipo.workbench.common.model.tool;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.resource.ResourceIdentifier;
import eu.slipo.workbench.common.model.resource.ResourceMetadataCreate;
import eu.slipo.workbench.common.model.tool.output.EnumOutputType;

/**
 * Represent configuration for registration to catalog
 */
public class MetadataRegistrationConfiguration implements ToolConfiguration 
{
    private static final long serialVersionUID = 1L;
    
    /**
     * A bunch of metadata to accompany the resource 
     */
    private ResourceMetadataCreate metadata;
    
    /**
     * A resource identifier to target an existing catalog resource to be updated
     * (by adding a new revision).
     */
    private ResourceIdentifier target;
    
    public MetadataRegistrationConfiguration() {}
    
    public MetadataRegistrationConfiguration(
        ResourceMetadataCreate metadata, ResourceIdentifier resourceIdentifier) 
    {
        this.metadata = metadata;
        this.target = resourceIdentifier;
    }

    public MetadataRegistrationConfiguration(ResourceMetadataCreate metadata)
    {
        this(metadata, null);
    }
    
    @JsonProperty("metadata")
    public ResourceMetadataCreate getMetadata() 
    {
        return metadata;
    }

    @JsonProperty("metadata")
    public void setMetadata(ResourceMetadataCreate metadata)
    {
        this.metadata = metadata;
    }
    
    @JsonProperty("target")
    public ResourceIdentifier getTarget()
    {
        return target;
    }
    
    @JsonProperty("target")
    public void setTarget(ResourceIdentifier target)
    {
        this.target = target;
    }
    
    @JsonIgnore
    @Override
    public EnumTool getTool()
    {
        return EnumTool.REGISTER;
    }

    @Override
    public EnumDataFormat getInputFormat()
    {
        return null;
    }

    @Override
    public void setInputFormat(EnumDataFormat inputFormat)
    {
        // no-op
    }

    @Override
    public List<String> getInput()
    {
        return Collections.emptyList();
    }

    @Override
    public void setInput(List<String> input)
    {
        // no-op
    }

    @Override
    public String getOutputDir()
    {
        return null; // no output
    }

    @Override
    public void setOutputDir(String dir)
    {
        // no-op
    }

    @Override
    public EnumDataFormat getOutputFormat()
    {
        return null; // no output is produced
    }

    @Override
    public void setOutputFormat(EnumDataFormat dataFormat)
    {
        // no-op
    }

    @Override
    public Map<EnumOutputType, List<String>> getOutputNames()
    {
        return Collections.emptyMap(); // no output is produced
    }
}
