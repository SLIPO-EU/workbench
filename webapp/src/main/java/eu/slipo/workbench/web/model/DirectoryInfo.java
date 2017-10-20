package eu.slipo.workbench.web.model;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DirectoryInfo {

    private String name;

    private String path;

    private ZonedDateTime createdOn;

    private List<FileInfo> files = new ArrayList<FileInfo>();

    private List<DirectoryInfo> folders = new ArrayList<DirectoryInfo>();

    public DirectoryInfo(String name, String path, ZonedDateTime createdOn) {
        this.name = name;
        this.path = path;
        this.createdOn = createdOn;
    }

    public DirectoryInfo(String name, String path, ZonedDateTime createdOn, List<FileInfo> files, List<DirectoryInfo> folders) {
        this.name = name;
        this.path = path;
        this.createdOn = createdOn;
        this.files.addAll(files);
        this.folders.addAll(folders);
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public ZonedDateTime getCreatedOn() {
        return createdOn;
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

    public int getSize() {
        return files.stream().mapToInt(f -> f.getSize()).sum();
    }

    public void addFile(FileInfo fi) {
        // TODO: Check folder name collisions
        Optional<FileInfo> existing = files.stream().filter(f -> f.getName().equalsIgnoreCase(fi.getName())).findFirst();
        if (existing.isPresent()) {
            this.files.remove(existing.get());
        }
        this.files.add(fi);
    }

    public void addFolder(DirectoryInfo di) {
        // TODO: Check file name collisions
        Optional<DirectoryInfo> existing = folders.stream().filter(d -> d.getName().equalsIgnoreCase(di.getName())).findFirst();
        if (existing.isPresent()) {
            this.folders.remove(existing.get());
        }
        this.folders.add(di);
    }

}
