package eu.slipo.workbench.common.model;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZonedDateTime;

/**
 * A file system entry for a file
 */
public class FileInfo extends FileSystemEntry {

    private static final long serialVersionUID = 1L;

    public FileInfo(String name, String path, long size, ZonedDateTime modifiedOn) 
    {
        super(name, path, size, modifiedOn);
    }
    
    public FileInfo(String name, String path, long size, long modifiedOn) 
    {
        super(name, path, size, modifiedOn);
    }
    
    public FileInfo(String name, String path, BasicFileAttributes attrs)
    {
        super(name, path, attrs);
    }
    
    public FileInfo(Path path, long size, long modifiedOn)
    {
        this(path.getFileName().toString(), path.toString(), size, modifiedOn);
    }
    
    public FileInfo(Path path, BasicFileAttributes attrs)
    {
        this(path.getFileName().toString(), path.toString(), attrs);
    }
}
