package eu.slipo.workbench.web.model.resource;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.slipo.workbench.common.model.resource.ResourceMetadataCreate;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;

/**
 * Resource registration request
 */
public class RegistrationRequest 
{
    @JsonAlias({ "configuration", "settings" })
    private TriplegeoConfiguration configuration;

    @JsonProperty("metadata")
    private ResourceMetadataCreate metadata;

    protected RegistrationRequest() {}
    
    public RegistrationRequest(TriplegeoConfiguration configuration, ResourceMetadataCreate metadata)
    {
        this.configuration = configuration;
        this.metadata = metadata;
    }

    /**
     * TripleGeo configuration settings for transforming resource data
     *
     * @return the configuration
     */
    @JsonProperty("configuration")
    public TriplegeoConfiguration getConfiguration() 
    {
        return configuration;
    }

    /**
     * Metadata for registering the resource to the catalog
     *
     * @return the metadata
     */
    @JsonProperty("metadata")
    public ResourceMetadataCreate getMetadata() 
    {
        return metadata;
    }

}
