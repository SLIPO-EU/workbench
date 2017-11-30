package eu.slipo.workbench.web.model.resource;

import eu.slipo.workbench.web.model.process.TripleGeoSettings;

/**
 * Resource registration request
 */
public class RegistrationRequest {

    private TripleGeoSettings settings;

    private ResourceMetadataCreate metadata;

    /**
     * TripleGeo configuration settings for processing resource data
     *
     * @return the configuration
     */
    public TripleGeoSettings getSettings() {
        return settings;
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
