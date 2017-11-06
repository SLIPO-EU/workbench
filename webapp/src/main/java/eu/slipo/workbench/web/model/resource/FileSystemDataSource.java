package eu.slipo.workbench.web.model.resource;

/**
 * File system data source
 */
public class FileSystemDataSource extends DataSource {

    private String path;

    public FileSystemDataSource() {
        super(EnumDataSource.FILESYSTEM);
    }

    /**
     * Relative path to the resource file on the shared storage
     *
     * @return the path
     */
    public String getPath() {
        return path;
    }

}
