package eu.slipo.workbench.common.model.process;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Query for searching processes
 */
public class ProcessQuery {
    /**
     * Search processes by name using LIKE SQL operator
     */
    private String name;

    /**
     * Search process by task
     */
    private EnumProcessTaskType taskType;

    /**
     * Search for templates
     */
    private Boolean template;

    /**
     * Search by the ID of the user that created the process
     */
    private Integer createdBy;

    /**
     * Exclude processes created by API calls
     */
    private boolean excludeApi;

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

    public EnumProcessTaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(EnumProcessTaskType t) {
        this.taskType = t;
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
