package eu.slipo.workbench.web.model.api;

public class CatalogInput extends Input {

    private static final long serialVersionUID = 1L;

    private long id;
    private long version;

    public CatalogInput() {
        super(EnumType.CATALOG);
    }

    public CatalogInput(long id, int version) {
        super(EnumType.CATALOG);

        this.id = id;
        this.version = version;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

}
