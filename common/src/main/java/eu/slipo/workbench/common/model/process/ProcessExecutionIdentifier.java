package eu.slipo.workbench.common.model.process;

import java.io.Serializable;
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represent an identifier for a persisted process execution
 */
public class ProcessExecutionIdentifier implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long processId;

    private final long processVersion;

    private final long executionId;

    @JsonIgnore
    private final int h;

    @JsonCreator
    public ProcessExecutionIdentifier(
        @JsonProperty("id") long id, @JsonProperty("version") long version, @JsonProperty("execution") long execution
    ) {
        this.processId = id;
        this.processVersion = version;
        this.executionId = execution;

        this.h = Arrays.hashCode(new long[] { id, version, execution });
    }

    public ProcessExecutionIdentifier(ProcessExecutionIdentifier other) {
        this(other.processId, other.processVersion, other.executionId);
    }

    @JsonProperty("id")
    public long getProcessId() {
        return processId;
    }

    @JsonProperty("version")
    public long getProcessVersion() {
        return processVersion;
    }

    @JsonProperty("execution")
    public long getExecutionId() {
        return executionId;
    }

    public static ProcessExecutionIdentifier of(long processId, long processVersion, long executionId) {
        return new ProcessExecutionIdentifier(processId, processVersion, executionId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !(obj instanceof ProcessExecutionIdentifier)) {
            return false;
        }
        ProcessExecutionIdentifier x = (ProcessExecutionIdentifier) obj;
        return x.processId == processId && x.processVersion == processVersion && x.executionId == executionId;
    }

    @Override
    public int hashCode() {
        return h;
    }

}
