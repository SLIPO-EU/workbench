package eu.slipo.workbench.web.model.process;

/**
 * Process execution status. Values are the same to
 * {@link org.springframework.batch.core.BatchStatus}
 */
public enum EnumProcessExecutionStatus {
    ABANDONED,
    COMPLETED,
    FAILED,
    STARTED,
    STARTING,
    STOPPED,
    STOPPING,
    UNKNOWN,
    ;

    public static EnumProcessExecutionStatus fromString(String value) {
        for (EnumProcessExecutionStatus item : EnumProcessExecutionStatus.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return EnumProcessExecutionStatus.UNKNOWN;
    }

}
