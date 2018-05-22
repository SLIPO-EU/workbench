package eu.slipo.workbench.common.model;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Represents a file system entry. An entry can be either a file or a directory
 */
public abstract class FileSystemEntry {

    private long size;

    private String path;

    private String name;

    private ZonedDateTime modifiedOn;

    protected FileSystemEntry(long size, String name, String path, ZonedDateTime modifiedOn) 
    {
        this.size = size;
        this.name = name;
        this.path = path;
        this.modifiedOn = modifiedOn;
    }
    
    protected FileSystemEntry(long size, String name, String path, long modifiedOn) 
    {
        this.size = size;
        this.name = name;
        this.path = path;
        
        Instant t = Instant.ofEpochMilli(modifiedOn);
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
