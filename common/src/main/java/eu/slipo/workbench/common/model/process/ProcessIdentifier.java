package eu.slipo.workbench.common.model.process;

public class ProcessIdentifier {

    private long id;

    private long version;

    public ProcessIdentifier(long id, long version) {
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
    public ProcessIdentifier clone() {
        return new ProcessIdentifier(this.id, this.version);
    }
}
