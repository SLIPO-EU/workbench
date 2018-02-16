package eu.slipo.workbench.common.model.process;

/**
 * A process execution status for reporting purposes.
 * 
 * <p>If this status accompanies a {@link ProcessExecutionRecord}, it represents 
 * the aggregate status of the entire process execution (i.e of the underlying workflow).
 * 
 * <p>Else, if this status accompanies a {@link ProcessExecutionStepRecord}, it represents
 * a simplified status derived from the underlying Spring-Batch job execution status.
 */
public enum EnumProcessExecutionStatus
{
    UNKNOWN,
    COMPLETED,
    FAILED,
    RUNNING,
    STOPPED,
    ;

    public static EnumProcessExecutionStatus fromString(String value)
    {
        for (EnumProcessExecutionStatus item : EnumProcessExecutionStatus.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return EnumProcessExecutionStatus.UNKNOWN;
    }

}
