package eu.slipo.workbench.web.model.resource;

import eu.slipo.workbench.web.model.process.TripleGeoConfiguration;

/**
 * Resource registration request
 */
public class ResourceRegistrationRequest {

    private DataSource dataSource;

    private TripleGeoConfiguration configuration;

    private ResourceMetadataCreate metadata;

    /**
     * Resource data source
     *
     * @return the data source description
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * TripleGeo configuration for processing resource data
     *
     * @return the configuration
     */
    public TripleGeoConfiguration getConfiguration() {
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
