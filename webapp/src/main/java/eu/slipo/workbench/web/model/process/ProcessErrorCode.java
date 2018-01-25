package eu.slipo.workbench.web.model.process;

import eu.slipo.workbench.common.model.ErrorCode;

/**
 * Error codes for process operations
 */
public enum ProcessErrorCode implements ErrorCode {
    UNKNOWN,
    QUERY_IS_EMPTY,
    NOT_FOUND,
    ;

    @Override
    public String key() {
        return (this.getClass().getSimpleName() + '.' + name());
    }

}
