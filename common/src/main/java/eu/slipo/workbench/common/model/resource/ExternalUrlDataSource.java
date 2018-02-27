package eu.slipo.workbench.common.model.resource;

import java.net.MalformedURLException;
import java.net.URL;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represent a data source from a public URL
 */
public class ExternalUrlDataSource extends DataSource 
{
    private static final long serialVersionUID = 1L;
    
    private URL url;

    protected ExternalUrlDataSource() 
    {
        super(EnumDataSourceType.EXTERNAL_URL);
    }
    
    public ExternalUrlDataSource(URL url) 
    {
        super(EnumDataSourceType.EXTERNAL_URL);
        this.url = url;
    }
    
    public ExternalUrlDataSource(String url)
    {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("The given URL is invalid", e);
        }
    }

    @JsonProperty("url")
    public URL getUrl() {
        return url;
    }

    @Override
    public String toString()
    {
        return String.format("ExternalUrlDataSource [url=%s]", url);
    }
    
    @Override
    public int hashCode()
    {
        return url == null? 0 : url.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null || !(obj instanceof ExternalUrlDataSource))
            return false;
        ExternalUrlDataSource other = (ExternalUrlDataSource) obj;
        return url == null? (other.url == null) : url.equals(other.url);
    }
}
