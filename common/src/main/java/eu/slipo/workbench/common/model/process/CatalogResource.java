package eu.slipo.workbench.common.model.process;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.slipo.workbench.common.model.poi.EnumResourceType;
import eu.slipo.workbench.common.model.resource.ResourceIdentifier;

/**
 * A process input resource that already exists in the catalog
 */
public class CatalogResource extends ProcessInput {

    private ResourceIdentifier resource;

    private String description;

    protected CatalogResource() 
    {
        super(-1, EnumInputType.CATALOG, EnumResourceType.UNDEFINED, null);
    }

    public CatalogResource(int key, EnumResourceType resourceType, String title, long id, long version) 
    {
        super(key, EnumInputType.CATALOG, resourceType, title);
        this.resource = new ResourceIdentifier(id, version);
    }

    public CatalogResource(
        int key, EnumResourceType resourceType, String title, long id, long version, String description) 
    {
        super(key, EnumInputType.CATALOG, resourceType, title);
        this.resource = new ResourceIdentifier(id, version);
        this.description = description;
    }

    public CatalogResource(
        int key, EnumResourceType resourceType, String title, ResourceIdentifier resourceIdentifier) 
    {
        super(key, EnumInputType.CATALOG, resourceType, title);
        this.resource = new ResourceIdentifier(resourceIdentifier);
    }

    public CatalogResource(
        int key, EnumResourceType resourceType, String title, ResourceIdentifier resource, String description)
    {
        super(key, EnumInputType.CATALOG, resourceType, title);
        this.resource = new ResourceIdentifier(resource);
        this.description = description;
    }

    @JsonIgnore()
    public long getId() {
        return this.resource.getId();
    }

    @JsonIgnore()
    public long getVersion() {
        return this.resource.getVersion();
    }

    public ResourceIdentifier getResource() {
        return resource;
    }

    public void setResource(ResourceIdentifier resource) {
        this.resource = resource;
    }

    public String getDescription() {
        return description;
    }

}
