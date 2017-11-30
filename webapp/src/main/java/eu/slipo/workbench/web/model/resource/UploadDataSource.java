package eu.slipo.workbench.web.model.resource;

/**
 * Uploaded file data source
 */
public class UploadDataSource extends DataSource {

    private String filename;

    public UploadDataSource() {
        super(EnumDataSource.UPLOAD);
    }

    public UploadDataSource(String filename) {
        super(EnumDataSource.UPLOAD);
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
