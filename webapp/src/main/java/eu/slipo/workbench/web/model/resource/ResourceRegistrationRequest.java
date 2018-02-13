package eu.slipo.workbench.web.model.resource;

import com.fasterxml.jackson.annotation.JsonProperty;

import eu.slipo.workbench.common.model.resource.DataSource;
import eu.slipo.workbench.common.model.resource.ResourceMetadataCreate;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;

/**
 * Resource registration request
 */
public class ResourceRegistrationRequest extends RegistrationRequest 
{
    @JsonProperty("dataSource")
    private DataSource dataSource;

    protected ResourceRegistrationRequest() {}    
        
    public ResourceRegistrationRequest(
        TriplegeoConfiguration configuration, ResourceMetadataCreate metadata, DataSource dataSource)
    {
        super(configuration, metadata);
        this.dataSource = dataSource;
    }

    /**
     * Resource data source
     *
     * @return the data source description
     */
    @JsonProperty("dataSource")
    public DataSource getDataSource() 
    {
        return dataSource;
    }

}
