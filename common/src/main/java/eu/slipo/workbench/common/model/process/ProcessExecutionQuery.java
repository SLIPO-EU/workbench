package eu.slipo.workbench.common.model.process;

/**
 * Query for searching process executions
 */
public class ProcessExecutionQuery {

    /**
     * Search executions by process name using LIKE SQL operator
     */
    private String name;

    /**
     * Search execution by task
     */
    private EnumProcessTaskType taskType;

    /**
     * Search execution by status
     */
    private EnumProcessExecutionStatus status;

    /**
     * Search by the ID of the user that created the process
     */
    private Integer createdBy;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EnumProcessTaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(EnumProcessTaskType task) {
        this.taskType = task;
    }

    public EnumProcessExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(EnumProcessExecutionStatus status) {
        this.status = status;
    }

    public Integer getCreatedBy()
    {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy)
    {
        this.createdBy = createdBy;
    }
}
