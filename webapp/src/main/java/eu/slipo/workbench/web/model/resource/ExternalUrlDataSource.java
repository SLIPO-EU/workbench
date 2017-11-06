package eu.slipo.workbench.web.model.resource;

/**
 * External URL data source
 */
public class ExternalUrlDataSource extends DataSource {

    private String url;

    public ExternalUrlDataSource() {
        super(EnumDataSource.EXTERNAL_URL);
    }

    /**
     * URL for downloading the resource
     *
     * @return the URL
     */
    public String getUrl() {
        return url;
    }

}
