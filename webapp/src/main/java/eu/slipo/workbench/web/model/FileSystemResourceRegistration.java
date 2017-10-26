package eu.slipo.workbench.web.model;

public class FileSystemResourceRegistration extends ResourceRegistration {

    /**
     * Path to the resource file on the shared storage
     */
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
