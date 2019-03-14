package eu.slipo.workbench.common.model.process;

import eu.slipo.workbench.common.model.poi.EnumOperation;

/**
 * Query for searching processes for API calls
 */
public class ApiCallQuery {

    /**
     * Search application key by name using LIKE SQL operator
     */
    private String name;

    /**
     * Search calls by operation type
     */
    private EnumOperation operation;

    /**
     * Search by status
     */
    private EnumProcessExecutionStatus status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EnumOperation getOperation() {
        return operation;
    }

    public void setOperation(EnumOperation operation) {
        this.operation = operation;
    }

    public EnumProcessExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(EnumProcessExecutionStatus status) {
        this.status = status;
    }

}
