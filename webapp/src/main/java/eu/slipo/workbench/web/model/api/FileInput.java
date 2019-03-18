package eu.slipo.workbench.web.model.api;

public class FileInput extends Input {

    private static final long serialVersionUID = 1L;

    private String path;

    public FileInput() {
        super(EnumType.FILESYSTEM);
    }

    protected FileInput(String path) {
        super(EnumType.FILESYSTEM);
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
