package eu.slipo.workbench.common.model.resource;

/**
 * File system data source
 */
public class FileSystemDataSource extends DataSource {

    private String path;

    public FileSystemDataSource() {
        super(EnumDataSourceType.FILESYSTEM);
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
