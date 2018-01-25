package eu.slipo.workbench.web.model.resource;

import eu.slipo.workbench.common.model.ErrorCode;

/**
 * Error codes for resource operations
 */
public enum ResourceErrorCode implements ErrorCode {
    UNKNOWN,
    QUERY_IS_EMPTY,
    DATASOURCE_NOT_SUPPORTED,
    ;

    @Override
    public String key() {
        return (this.getClass().getSimpleName() + '.' + name());
    }

}
