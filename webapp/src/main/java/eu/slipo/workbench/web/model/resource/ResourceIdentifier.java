package eu.slipo.workbench.web.model.resource;

/**
 * Unique resource identifier
 */
public class ResourceIdentifier {

    private long id;

    private long version;

    protected ResourceIdentifier() {

    }

    public ResourceIdentifier(long id, long version) {
        this.id = id;
        this.version = version;
    }

    public long getId() {
        return id;
    }

    public long getVersion() {
        return version;
    }

    @Override
    public ResourceIdentifier clone() {
        return new ResourceIdentifier(id, version);
    }

}
