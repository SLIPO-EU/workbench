package eu.slipo.workbench.web.model.process;

/**
 * A temporary file used as a process input resource
 */
public class FileProcessResource extends ProcessResource {

    private String filename;

    private TripleGeoConfiguration configuration;

    public FileProcessResource() {
        super(EnumProcessResource.FILE);
    }

    public FileProcessResource(int index, String filename, TripleGeoConfiguration configuration) {
        super(index, EnumProcessResource.FILE);
        this.filename = filename;
        this.configuration = configuration;
    }

    public String getFilename() {
        return this.filename;
    }

    public TripleGeoConfiguration getConfiguration() {
        return configuration;
    }

}
