package eu.slipo.workbench.web.model.provenance;

/**
 * Request for a single POI provenance data
 */
public class ProvenanceRequest {

    private long executionId;
    private long processId;
    private long processVersion;
    private long id;
    private String outputKey;
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

    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
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
