package eu.slipo.workbench.web.model;

public class UploadResourceRegistration extends ResourceRegistration {

    /**
     * Index of the uploaded file
     */
    private int fileIndex;

    public UploadResourceRegistration() {
        super();
        this.source = EnumDataSource.UPLOAD;
    }

    public int getFileIndex() {
        return fileIndex;
    }

    public void setFileIndex(int fileIndex) {
        this.fileIndex = fileIndex;
    }

}
