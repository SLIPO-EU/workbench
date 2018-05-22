package eu.slipo.workbench.common.model.resource;

import java.net.MalformedURLException;
import java.net.URL;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represent a data source from a public URL
 */
public class UrlDataSource extends DataSource 
{
    private static final long serialVersionUID = 1L;
    
    private URL url;

    protected UrlDataSource() 
    {
        super(EnumDataSourceType.URL);
    }
    
    public UrlDataSource(URL url) 
    {
        super(EnumDataSourceType.URL);
        this.url = url;
    }
    
    public UrlDataSource(String url)
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
        return String.format("UrlDataSource [url=%s]", url);
    }
    
    @Override
    public int hashCode()
    {
        return url == null? 0 : url.toString().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null || !(obj instanceof UrlDataSource))
            return false;
        UrlDataSource other = (UrlDataSource) obj;
        return url == null? (other.url == null) : url.equals(other.url);
    }
}
