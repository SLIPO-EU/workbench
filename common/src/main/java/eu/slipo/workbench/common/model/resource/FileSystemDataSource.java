package eu.slipo.workbench.common.model.resource;

import java.nio.file.Path;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * File system data source
 */
public class FileSystemDataSource extends DataSource 
{
    private static final long serialVersionUID = 1L;
    
    @JsonProperty("path")
    private String path;

    public FileSystemDataSource() 
    {
        super(EnumDataSourceType.FILESYSTEM);
    }
    
    public FileSystemDataSource(String path) 
    {
        super(EnumDataSourceType.FILESYSTEM);
        this.path = path;
    }

    public FileSystemDataSource(Path path) 
    {
        super(EnumDataSourceType.FILESYSTEM);
        this.path = path.toString();
    }
    
    /**
     * Relative path to the resource file on the shared storage
     *
     * @return the path
     */
    @JsonProperty("path")
    public String getPath() 
    {
        return path;
    }

    @Override
    public String toString()
    {
        return String.format("FileSystemDataSource [path=%s]", path);
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
        if (obj == null || !(obj instanceof FileSystemDataSource))
            return false;
        FileSystemDataSource other = (FileSystemDataSource) obj;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        return true;
    }
}
