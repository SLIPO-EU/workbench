package eu.slipo.workbench.common.model.tool;

import com.fasterxml.jackson.annotation.JsonProperty;

import eu.slipo.workbench.common.model.resource.ResourceIdentifier;
import eu.slipo.workbench.common.model.resource.ResourceMetadataCreate;

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
}
