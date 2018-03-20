package eu.slipo.workbench.common.model;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A file system entry for a directory
 */
public class DirectoryInfo extends FileSystemEntry {

    private List<FileInfo> files = new ArrayList<FileInfo>();

    private List<DirectoryInfo> folders = new ArrayList<DirectoryInfo>();

    public DirectoryInfo(String name, String path, ZonedDateTime createdOn) {
        super(0, name, path, createdOn);
    }

    public DirectoryInfo(String name, String path, ZonedDateTime createdOn, List<FileInfo> files, List<DirectoryInfo> folders) {
        super(0, name, path, createdOn);

        this.files.addAll(files);
        this.folders.addAll(folders);
    }

    public List<FileInfo> getFiles() {
        return Collections.unmodifiableList(files);
    }

    public List<DirectoryInfo> getFolders() {
        return Collections.unmodifiableList(folders);
    }

    public int getCount() {
        return (files.size() + folders.size());
    }

    @Override
    public long getSize() {
        return files.stream().mapToLong(f -> f.getSize()).sum();
    }

    public void addFile(FileInfo fi) {
        Optional<FileInfo> existing = files.stream().filter(f -> f.getName().equalsIgnoreCase(fi.getName())).findFirst();
        if (existing.isPresent()) {
            this.files.remove(existing.get());
        }
        this.files.add(fi);
    }

    public void addFolder(DirectoryInfo di) {
        Optional<DirectoryInfo> existing = folders.stream().filter(d -> d.getName().equalsIgnoreCase(di.getName())).findFirst();
        if (existing.isPresent()) {
            this.folders.remove(existing.get());
        }
        this.folders.add(di);
    }

}
