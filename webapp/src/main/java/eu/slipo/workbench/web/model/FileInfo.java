package eu.slipo.workbench.web.model;

import java.time.ZonedDateTime;

/**
 * The file system entry of a file
 */
public class FileInfo extends FileSystemEntry {

    public FileInfo(int size, String name, String path, ZonedDateTime createdOn) {
        super(size, name, path, createdOn);
    }

}
