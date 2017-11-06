package eu.slipo.workbench.web.model;

import java.time.ZonedDateTime;

/**
 * A file system entry for a file
 */
public class FileInfo extends FileSystemEntry {

    public FileInfo(int size, String name, String path, ZonedDateTime createdOn) {
        super(size, name, path, createdOn);
    }

}
