package eu.slipo.workbench.web.model.process;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import eu.slipo.workbench.web.model.resource.DataSource;
import eu.slipo.workbench.web.model.resource.ExternalUrlDataSource;
import eu.slipo.workbench.web.model.resource.FileSystemDataSource;

/**
 * A transient resource that is extracted dynamically from a data source
 */
public class TransientProcessResource extends ProcessResource {

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
    @JsonSubTypes({
        @Type(value = FileSystemDataSource.class, name = "FILESYSTEM"),
        @Type(value = ExternalUrlDataSource.class, name = "EXTERNAL_URL"),
    })
    private DataSource dataSource;

    private TripleGeoConfiguration configuration;

    public TransientProcessResource() {
        super();
        this.type = EnumProcessResource.TRANSIENT;
    }

    public TransientProcessResource(int index, DataSource dataSource, TripleGeoConfiguration configuration) {
        super(index, EnumProcessResource.TRANSIENT);
        this.dataSource = dataSource;
        this.configuration = configuration;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public TripleGeoConfiguration getConfiguration() {
        return configuration;
    }

}
