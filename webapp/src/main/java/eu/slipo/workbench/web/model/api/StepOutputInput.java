package eu.slipo.workbench.web.model.api;

public class StepOutputInput extends Input {

    private static final long serialVersionUID = 1L;

    private long processId;
    private long processVersion;
    private long fileId;

    public StepOutputInput() {
        super(EnumType.OUTPUT);
    }

    public StepOutputInput(long processId, long processVersion, int fileId) {
        super(EnumType.OUTPUT);

        this.processId = processId;
        this.processId = processId;
        this.fileId = fileId;
    }

    public long getProcessId() {
        return processId;
    }

    public void setProcessId(long processId) {
        this.processId = processId;
    }

    public long getProcessVersion() {
        return processVersion;
    }

    public void setProcessVersion(long processVersion) {
        this.processVersion = processVersion;
    }

    public long getFileId() {
        return fileId;
    }

    public void setFileId(long fileId) {
        this.fileId = fileId;
    }

}
