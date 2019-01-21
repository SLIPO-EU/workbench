package eu.slipo.workbench.web.service.etl;

import java.nio.file.Path;

public class ExportFile {

    private Path path;

    public ExportFile(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

}