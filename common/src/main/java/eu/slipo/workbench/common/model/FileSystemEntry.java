package eu.slipo.workbench.common.model;

import java.io.Serializable;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Represents a file system entry. An entry can be either a file or a directory
 */
public abstract class FileSystemEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private long size;

    private String path;

    private String name;

    private ZonedDateTime modifiedOn;

    protected FileSystemEntry(String name, String path, long size, ZonedDateTime modifiedOn) 
    {
        this.name = name;
        this.path = path;
        this.size = size;
        this.modifiedOn = modifiedOn;
    }
    
    protected FileSystemEntry(String name, String path, long size, long modifiedOn) 
    {
        this.name = name;
        this.path = path;
        this.size = size;
        
        Instant t = Instant.ofEpochMilli(modifiedOn);
        this.modifiedOn = ZonedDateTime.ofInstant(t, ZoneOffset.UTC);
    }
    
    protected FileSystemEntry(String name, String path, BasicFileAttributes attrs)
    {
        this.name = name;
        this.path = path;
        this.size = attrs.size();
        
        Instant t = attrs.lastModifiedTime().toInstant();
        this.modifiedOn = ZonedDateTime.ofInstant(t, ZoneOffset.UTC);
    }

    public long getSize() 
    {
        return size;
    }

    public String getPath() 
    {
        return path;
    }

    public String getName() 
    {
        return name;
    }

    public ZonedDateTime getModified() 
    {
        return modifiedOn;
    }

}
