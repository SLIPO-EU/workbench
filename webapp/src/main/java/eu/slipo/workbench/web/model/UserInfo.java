package eu.slipo.workbench.web.model;

public class UserInfo {

    private long id;

    private String name;

    public UserInfo(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {

        return id;
    }

    public String getName() {
        return name;
    }

}
