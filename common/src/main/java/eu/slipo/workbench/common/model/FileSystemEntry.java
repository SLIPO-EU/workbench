package eu.slipo.workbench.common.model;

import java.time.ZonedDateTime;

/**
 * Represents a file system entry. An entry can be either a file or a directory
 */
public abstract class FileSystemEntry {

    private long size;

    private String path;

    private String name;

    private ZonedDateTime createdOn;

    protected FileSystemEntry(long size, String name, String path, ZonedDateTime createdOn) {
        this.size = size;
        this.name = name;
        this.path = path;
        this.createdOn = createdOn;
    }

    public long getSize() {
        return size;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public ZonedDateTime getCreatedOn() {
        return createdOn;
    }

}
