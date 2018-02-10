package eu.slipo.workbench.web.model.resource;

import eu.slipo.workbench.common.model.resource.DataSource;

/**
 * Resource registration request
 */
public class ResourceRegistrationRequest extends RegistrationRequest {

    private DataSource dataSource;

    /**
     * Resource data source
     *
     * @return the data source description
     */
    public DataSource getDataSource() {
        return dataSource;
    }

}
