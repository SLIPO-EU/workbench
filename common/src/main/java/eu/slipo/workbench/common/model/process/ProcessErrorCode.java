package eu.slipo.workbench.common.model.process;

import eu.slipo.workbench.common.model.ErrorCode;

/**
 * Error codes for process operations
 */
public enum ProcessErrorCode implements ErrorCode
{
    UNKNOWN,
    INVALID,
    NAME_DUPLICATE,
    PROCESS_NOT_FOUND,
    EXECUTION_NOT_FOUND,
    QUERY_IS_EMPTY,
    FAILED_TO_START,
    FAILED_TO_STOP,
    RPC_SERVER_UNREACHABLE,
    ;

    @Override
    public String key() {
        return (this.getClass().getSimpleName() + '.' + name());
    }

}
