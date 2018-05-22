package eu.slipo.workbench.common.model;

import java.time.ZonedDateTime;

/**
 * A file system entry for a file
 */
public class FileInfo extends FileSystemEntry {

    public FileInfo(long size, String name, String path, ZonedDateTime modifiedOn) 
    {
        super(size, name, path, modifiedOn);
    }
    
    public FileInfo(long size, String name, String path, long modifiedOn) 
    {
        super(size, name, path, modifiedOn);
    }
}
