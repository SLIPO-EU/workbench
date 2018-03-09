package eu.slipo.workbench.common.model.process;

import java.io.Serializable;
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represent an identifier for a persisted process
 */
public class ProcessIdentifier implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final long id;

    private final long version;
    
    @JsonIgnore
    private final int h;
    
    @JsonCreator
    public ProcessIdentifier(
        @JsonProperty("id") long id, @JsonProperty("version") long version) 
    {
        this.id = id;
        this.version = version;
        
        this.h = Arrays.hashCode(new long[] { id, version }); 
    }
    
    public ProcessIdentifier(ProcessIdentifier other) 
    {
        this(other.id, other.version);
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
    
    public static ProcessIdentifier of(long id)
    {
        return new ProcessIdentifier(id, -1);
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
        if (obj == null || !(obj instanceof ProcessIdentifier))
            return false;
        ProcessIdentifier x = (ProcessIdentifier) obj;
        return x.id == id && x.version == version;
    }
    
    @Override
    public int hashCode()
    {
        return h;
    }
}
