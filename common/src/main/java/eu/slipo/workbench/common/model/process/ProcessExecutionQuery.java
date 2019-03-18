package eu.slipo.workbench.common.model.process;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Query for searching process executions
 */
public class ProcessExecutionQuery {

    /**
     * Search by process ID
     */
    private Long id;

    /**
     * Search by process version
     */
    private Long version;

    /**
     * Search by process name using LIKE SQL operator
     */
    private String name;

    /**
     * Search by task type
     */
    private EnumProcessTaskType taskType;

    /**
     * Search by status
     */
    private EnumProcessExecutionStatus status;

    /**
     * Search by the ID of the user that created the process
     */
    private Integer createdBy;

    private boolean excludeApi;

    public Long getId() {
        return id;
    }

    public void setId(Long processId) {
        this.id = processId;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

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

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    @JsonIgnore()
    public boolean isExcludeApi() {
        return excludeApi;
    }

    @JsonIgnore()
    public void setExcludeApi(boolean excludeApi) {
        this.excludeApi = excludeApi;
    }

}
