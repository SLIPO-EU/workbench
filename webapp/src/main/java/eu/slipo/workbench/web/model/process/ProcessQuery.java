package eu.slipo.workbench.web.model.process;

import eu.slipo.workbench.web.model.Query;

/**
 * Query for searching processes
 */
public class ProcessQuery extends Query {

    /**
     * Search processes by name using LIKE SQL operator
     */
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
