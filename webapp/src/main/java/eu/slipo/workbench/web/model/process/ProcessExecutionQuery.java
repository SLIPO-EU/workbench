package eu.slipo.workbench.web.model.process;

import eu.slipo.workbench.web.model.Query;

/**
 * Query for searching process executions
 */
public class ProcessExecutionQuery extends Query {

    /**
     * Search executions by process name using LIKE SQL operator
     */
    private String name;

    /**
     * Search execution by task
     */
    private EnumProcessTask task;

    /**
     * Search execution by status
     */
    private EnumProcessExecutionStatus status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EnumProcessTask getTask() {
        return task;
    }

    public void setTask(EnumProcessTask task) {
        this.task = task;
    }

    public EnumProcessExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(EnumProcessExecutionStatus status) {
        this.status = status;
    }

}
