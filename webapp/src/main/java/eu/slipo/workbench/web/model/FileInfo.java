package eu.slipo.workbench.web.model;

import java.time.ZonedDateTime;

/**
 * Represents a file on the local file system
 */
public class FileInfo extends File {

    private String path;

    private ZonedDateTime createdOn;

    public FileInfo(int size, String name, String path, ZonedDateTime createdOn) {
        super(size, name);
        this.path = path;
        this.createdOn = createdOn;
    }

    public String getPath() {
        return path;
    }

    public ZonedDateTime getCreatedOn() {
        return createdOn;
    }

}
