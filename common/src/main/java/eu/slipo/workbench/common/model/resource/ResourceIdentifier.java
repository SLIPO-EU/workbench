package eu.slipo.workbench.common.model.resource;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Unique resource identifier
 */
public class ResourceIdentifier implements Serializable {

    private long id;

    private long version;

    protected ResourceIdentifier() {}

    @JsonCreator
    public ResourceIdentifier(
        @JsonProperty("id") long id, @JsonProperty("version") long version) 
    {
        this.id = id;
        this.version = version;
    }
    
    public ResourceIdentifier(ResourceIdentifier other)
    {
        this.id = other.id;
        this.version = other.version;
    }

    @JsonProperty("id")
    public long getId() {
        return id;
    }

    @JsonProperty("version")
    public long getVersion() {
        return version;
    }
    
    public static ResourceIdentifier of(long id, long version)
    {
        return new ResourceIdentifier(id, version);
    }

    @Override
    public String toString()
    {
        return String.format("%d@%d", id, version);
    }
}
