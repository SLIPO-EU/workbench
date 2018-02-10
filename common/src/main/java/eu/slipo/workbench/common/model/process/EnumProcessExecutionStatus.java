package eu.slipo.workbench.common.model.process;

/**
 * Process execution status. 
 * These values (apart from UNKNOWN) are the same to {@link WorkflowScheduler#ExecutionStatus}.
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
            if (item.name().equalsIgnoreCase(value))
                return item;
        }
        return EnumProcessExecutionStatus.UNKNOWN;
    }

}
