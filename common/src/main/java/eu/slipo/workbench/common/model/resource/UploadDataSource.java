package eu.slipo.workbench.common.model.resource;

/**
 * Uploaded file data source
 */
public class UploadDataSource extends DataSource {

    private String filename;

    public UploadDataSource() {
        super(EnumDataSourceType.UPLOAD);
    }

    public UploadDataSource(String filename) {
        super(EnumDataSourceType.UPLOAD);
        this.filename = filename;
    }

    /**
     * The uploaded filename
     *
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

}
