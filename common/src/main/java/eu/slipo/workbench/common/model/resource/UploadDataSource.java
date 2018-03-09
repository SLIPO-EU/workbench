package eu.slipo.workbench.common.model.resource;

import java.nio.file.Path;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Uploaded file data source
 */
public class UploadDataSource extends DataSource 
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("path")
    private String path;

    public UploadDataSource() 
    {
        super(EnumDataSourceType.UPLOAD);
    }

    public UploadDataSource(Path path) 
    {
        super(EnumDataSourceType.UPLOAD);
        this.path = path.toString();
    }

    /**
     * The uploaded filename
     *
     * @return the filename
     */
    @JsonProperty("path")
    public String getPath() 
    {
        return path;
    }

    @Override
    public String toString()
    {
        return String.format("UploadDataSource [path=%s]", path);
    }
    
    @Override
    public int hashCode()
    {
        return path == null? 0 : path.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null || !(obj instanceof UploadDataSource))
            return false;
        UploadDataSource other = (UploadDataSource) obj;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        return true;
    }
}
