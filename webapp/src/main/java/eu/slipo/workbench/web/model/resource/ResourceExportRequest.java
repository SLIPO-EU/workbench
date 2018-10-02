package eu.slipo.workbench.web.model.resource;

import com.fasterxml.jackson.annotation.JsonProperty;

import eu.slipo.workbench.common.model.process.CatalogResource;
import eu.slipo.workbench.common.model.tool.ReverseTriplegeoConfiguration;

/**
 * A catalog resource export request
 */
public class ResourceExportRequest {

    @JsonProperty("resource")
    private CatalogResource resource;

    @JsonProperty("configuration")
    private ReverseTriplegeoConfiguration configuration;

    protected ResourceExportRequest() {

    }

    public ResourceExportRequest(CatalogResource resource, ReverseTriplegeoConfiguration configuration) {
        this.resource = resource;
        this.configuration = configuration;
    }

    /**
     * Exported resource
     *
     * @return the exported catalog resource
     */
    @JsonProperty("resource")
    public CatalogResource getResource() {
        return resource;
    }

    /**
     * TripleGeo configuration settings for transforming resource data
     *
     * @return the configuration
     */
    @JsonProperty("configuration")
    public ReverseTriplegeoConfiguration getConfiguration() {
        return configuration;
    }

}
