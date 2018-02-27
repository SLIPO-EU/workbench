package eu.slipo.workbench.common.model.resource;

import java.io.Serializable;
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represent an identifier for catalog resource 
 */
public class ResourceIdentifier implements Serializable 
{
    private static final long serialVersionUID = 1L;

    private final long id;

    private final long version;
    
    @JsonIgnore
    private final int h;

    @JsonCreator
    public ResourceIdentifier(
        @JsonProperty("id") long id, @JsonProperty("version") long version) 
    {
        this.id = id;
        this.version = version;
        
        this.h = Arrays.hashCode(new long[] { id, version });
    }
    
    public ResourceIdentifier(ResourceIdentifier other)
    {
        this(other.id, other.version);
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
    
    public static ResourceIdentifier of(long id)
    {
        return new ResourceIdentifier(id, -1);
    }

    @Override
    public String toString()
    {
        return version < 0?
            String.format("%d@latest", id) : String.format("%d@%d", id, version);
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof ResourceIdentifier))
            return false;
        ResourceIdentifier x = (ResourceIdentifier) obj;
        return id == x.id && version == x.version; 
    }
    
    @Override
    public int hashCode()
    {
        return h;
    }
}
