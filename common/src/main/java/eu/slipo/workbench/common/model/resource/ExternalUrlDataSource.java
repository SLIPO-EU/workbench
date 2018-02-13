package eu.slipo.workbench.common.model.resource;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * External URL data source
 */
public class ExternalUrlDataSource extends DataSource 
{
    @JsonProperty("url")
    private String url;

    public ExternalUrlDataSource() 
    {
        super(EnumDataSourceType.EXTERNAL_URL);
    }
    
    public ExternalUrlDataSource(String url) 
    {
        super(EnumDataSourceType.EXTERNAL_URL);
        this.url = url;
    }

    /**
     * URL for downloading the resource
     *
     * @return the URL
     */
    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    @Override
    public String toString()
    {
        return String.format("ExternalUrlDataSource [url=%s]", url);
    }
}
