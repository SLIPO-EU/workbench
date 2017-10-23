package eu.slipo.workbench.web.model;

public class ValueListItem<Key> {

    private Key key;

    private String name;

    public ValueListItem(Key key, String name) {
        this.key = key;
        this.name = name;
    }

    public Key getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

}
