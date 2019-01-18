package eu.slipo.workbench.web.service.etl;

import java.nio.file.Path;

public class ResourceExportFile extends ExportFile {

    private long id;
    private long version;

    public ResourceExportFile(long id, long version, Path path) {
        super(path);

        this.id = id;
        this.version = version;
    }

    public long getId() {
        return id;
    }

    public long getVersion() {
        return version;
    }

}
