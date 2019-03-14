package eu.slipo.workbench.web.model.evolution;

/**
 * Request for a single POI evolution data
 */
public class EvolutionRequest {

    private long processId;

    private long processVersion;

    private long executionId;

    private long id;

    private String uri;

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

    public long getExecutionId() {
        return executionId;
    }

    public void setExecutionId(long executionId) {
        this.executionId = executionId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
