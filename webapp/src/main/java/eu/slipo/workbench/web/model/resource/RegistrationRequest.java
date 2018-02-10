package eu.slipo.workbench.web.model.resource;

import eu.slipo.workbench.common.model.resource.ResourceMetadataCreate;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;

/**
 * Resource registration request
 */
public class RegistrationRequest {

    private TriplegeoConfiguration configuration;

    private ResourceMetadataCreate metadata;

    /**
     * TripleGeo configuration settings for processing resource data
     *
     * @return the configuration
     */
    public TriplegeoConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Metadata for registering the resource to the catalog
     *
     * @return the metadata
     */
    public ResourceMetadataCreate getMetadata() {
        return metadata;
    }

}
