package eu.slipo.workbench.web.model.process;

import eu.slipo.workbench.web.model.resource.DataSource;

/**
 * A transient resource that is extracted dynamically from a data source
 */
public class TransientProcessResource extends ProcessResource {

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
