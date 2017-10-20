package eu.slipo.workbench.web.model;

public class FileSystemResourceRegistration extends ResourceRegistration {

    private String path;

    public FileSystemResourceRegistration() {
        super();
        this.source = EnumDataSource.FILESYSTEM;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
