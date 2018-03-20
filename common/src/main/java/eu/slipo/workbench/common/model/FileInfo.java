package eu.slipo.workbench.common.model;

import java.time.ZonedDateTime;

/**
 * A file system entry for a file
 */
public class FileInfo extends FileSystemEntry {

    public FileInfo(long size, String name, String path, ZonedDateTime createdOn) {
        super(size, name, path, createdOn);
    }

}
