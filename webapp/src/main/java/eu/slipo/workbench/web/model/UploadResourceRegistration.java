package eu.slipo.workbench.web.model;

import org.springframework.web.multipart.MultipartFile;

public class UploadResourceRegistration extends ResourceRegistration {

    private MultipartFile[] files;

    public UploadResourceRegistration() {
        super();
        this.source = EnumDataSource.UPLOAD;
    }

    public MultipartFile[] getFiles() {
        return files;
    }

    public void setFiles(MultipartFile[] files) {
        this.files = files;
    }
}
