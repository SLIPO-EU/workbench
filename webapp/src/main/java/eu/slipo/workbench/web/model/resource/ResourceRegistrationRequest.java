package eu.slipo.workbench.web.model.resource;

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
