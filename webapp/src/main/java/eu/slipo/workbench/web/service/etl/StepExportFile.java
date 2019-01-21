package eu.slipo.workbench.web.service.etl;

import java.nio.file.Path;

public class StepExportFile extends ExportFile {

    private int stepKey;
    private long fileId;

    public StepExportFile(int stepKey, long fileId, Path path) {
        super(path);

        this.stepKey = stepKey;
        this.fileId = fileId;
    }

    public int getStepKey() {
        return stepKey;
    }

    public long getFileId() {
        return fileId;
    }

}