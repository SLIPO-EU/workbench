package eu.slipo.workbench.web.model.process;

public class FileProcessResource extends ProcessResource {

    private String filename;

    public FileProcessResource() {
        super();
        this.type = EnumProcessResource.FILE;
    }

    public FileProcessResource(int index, String filename) {
        super(index, EnumProcessResource.FILE);
        this.filename = filename;
    }

    public String getFilename() {
        return this.filename;
    }

}
