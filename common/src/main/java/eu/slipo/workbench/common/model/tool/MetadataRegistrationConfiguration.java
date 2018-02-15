package eu.slipo.workbench.common.model.tool;

import eu.slipo.workbench.common.model.resource.ResourceMetadataCreate;

/**
 * Configuration for registration to catalog
 */
@SuppressWarnings("serial")
public class MetadataRegistrationConfiguration implements ToolConfiguration 
{
    private ResourceMetadataCreate metadata;
    
    public MetadataRegistrationConfiguration() {}
    
    public MetadataRegistrationConfiguration(ResourceMetadataCreate metadata) 
    {
        this.metadata = metadata;
    }

    public ResourceMetadataCreate getMetadata() 
    {
        return metadata;
    }

    public void setMetadata(ResourceMetadataCreate metadata)
    {
        this.metadata = metadata;
    }
}
