package eu.slipo.workbench.web.model.process;

public class CatalogProcessResource extends ProcessResource {

    private long id;

    private int version;

    public CatalogProcessResource() {
        super();
        this.type = EnumProcessResource.CATALOG;
    }

    public CatalogProcessResource(int index, long id, int version) {
        super(index, EnumProcessResource.CATALOG);
        this.id = id;
        this.version = version;
    }

    public long getId() {
        return id;
    }

    public int getVersion() {
        return version;
    }

}
