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

    /**
     * Search process by task
     */
    private EnumProcessTask task;

    /**
     * Search for templates
     */
    private Boolean template;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getTemplate() {
        return template;
    }

    public void setTemplate(Boolean template) {
        this.template = template;
    }

    public EnumProcessTask getTask() {
        return task;
    }

    public void setTask(EnumProcessTask task) {
        this.task = task;
    }

}
