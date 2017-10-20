package eu.slipo.workbench.web.model;

/**
 * A simple file representation
 */
public class File {

    private int size;

    private String name;

    public File(int size, String name) {
        this.size = size;
        this.name = name;
    }

    public int getSize() {
        return size;
    }

    public String getName() {
        return name;
    }

}
