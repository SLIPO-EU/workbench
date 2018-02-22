package eu.slipo.workbench.common.model.user;

import java.io.Serializable;

public class AccountInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private String name;

    public AccountInfo(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
