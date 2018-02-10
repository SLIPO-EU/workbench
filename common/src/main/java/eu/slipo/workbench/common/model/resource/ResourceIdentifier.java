package eu.slipo.workbench.common.model.resource;

/**
 * Unique resource identifier
 */
public class ResourceIdentifier {

    private long id;

    private long version;

    protected ResourceIdentifier() {}

    public ResourceIdentifier(long id, long version) {
        this.id = id;
        this.version = version;
    }
    
    public ResourceIdentifier(ResourceIdentifier other)
    {
        this.id = other.id;
        this.version = other.version;
    }

    public long getId() {
        return id;
    }

    public long getVersion() {
        return version;
    }
    
    public static ResourceIdentifier of(long id, long version)
    {
        return new ResourceIdentifier(id, version);
    }
}
