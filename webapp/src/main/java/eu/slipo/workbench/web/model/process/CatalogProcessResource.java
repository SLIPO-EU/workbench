package eu.slipo.workbench.web.model.process;

import eu.slipo.workbench.web.model.resource.ResourceIdentifier;

/**
 * A process input resource that already exists in the resource catalog
 */
public class CatalogProcessResource extends ProcessResource {

    private ResourceIdentifier resource;

    protected CatalogProcessResource() {
        super(EnumProcessResource.CATALOG);
    }

    public CatalogProcessResource(int index, long id, long version) {
        super(index, EnumProcessResource.CATALOG);
        this.resource = new ResourceIdentifier(id, version);
    }

    public CatalogProcessResource(int index, ResourceIdentifier resource) {
        super(index, EnumProcessResource.CATALOG);
        this.resource = resource.clone();
    }

    public long getId() {
        return this.resource.getId();
    }

    public long getVersion() {
        return this.resource.getVersion();
    }

}
