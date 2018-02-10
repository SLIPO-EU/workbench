package eu.slipo.workbench.common.model.tool;

import java.nio.file.Path;

import eu.slipo.workbench.common.model.resource.ResourceMetadataCreate;

/**
 * Configuration for registration to catalog
 */
@SuppressWarnings("serial")
public class MetadataRegistrationConfiguration implements ToolConfiguration 
{
    private Path input;
    
    private ResourceMetadataCreate metadata;
    
    public MetadataRegistrationConfiguration(ResourceMetadataCreate metadata) {
        this.metadata = metadata;
    }

    public ResourceMetadataCreate getMetadata() {
        return metadata;
    }

}
