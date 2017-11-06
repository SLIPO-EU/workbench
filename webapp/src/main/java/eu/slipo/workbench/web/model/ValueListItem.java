package eu.slipo.workbench.web.model;

/**
 * Generic key value item
 *
 * @param <Key> the key type
 */
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
