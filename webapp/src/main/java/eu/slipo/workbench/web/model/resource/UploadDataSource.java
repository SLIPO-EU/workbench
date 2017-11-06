package eu.slipo.workbench.web.model.resource;

/**
 * Uploaded file data source
 */
public class UploadDataSource extends DataSource {

    private int fileIndex;

    public UploadDataSource() {
        super(EnumDataSource.UPLOAD);
    }

    /**
     * Index of the uploaded file
     *
     * @return the index
     */
    public int getFileIndex() {
        return fileIndex;
    }

}
