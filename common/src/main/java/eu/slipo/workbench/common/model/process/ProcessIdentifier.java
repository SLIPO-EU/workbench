package eu.slipo.workbench.common.model.process;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProcessIdentifier implements Serializable
{
    private long id;

    private long version;

    protected ProcessIdentifier() {}
    
    @JsonCreator
    public ProcessIdentifier(
        @JsonProperty("id") long id, @JsonProperty("version") long version) 
    {
        this.id = id;
        this.version = version;
    }
    
    public ProcessIdentifier(ProcessIdentifier other) {
        this.id = other.id;
        this.version = other.version;
    }

    @JsonProperty("id")
    public long getId() 
    {
        return id;
    }

    @JsonProperty("version")
    public long getVersion() 
    {
        return version;
    }

    public static ProcessIdentifier of(long id, long version)
    {
        return new ProcessIdentifier(id, version);
    }
    
    @Override
    public String toString()
    {
        return String.format("%d@%d", id, version);
    }
}
