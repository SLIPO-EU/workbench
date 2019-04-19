package eu.slipo.workbench.common.model.process;

public class ProcessExecutionFileNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    public static ProcessExecutionFileNotFoundException forFile(long executionId, long fileId) {
        return new ProcessExecutionFileNotFoundException(executionId, fileId);
    }

    private ProcessExecutionFileNotFoundException(long executionId, long fileId) {
        super(String.format("File [%d] was not found for execution [%d]", fileId, executionId));
    }
}
