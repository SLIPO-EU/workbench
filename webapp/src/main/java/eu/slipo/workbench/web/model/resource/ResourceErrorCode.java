package eu.slipo.workbench.web.model.resource;

import eu.slipo.workbench.common.model.ErrorCode;

/**
 * Error codes for resource operations
 */
public enum ResourceErrorCode implements ErrorCode {
    UNKNOWN,
    DATASOURCE_NOT_SUPPORTED,
    FILE_NOT_FOUND,
    NAME_DUPLICATE,
    QUERY_IS_EMPTY,
    ;

    @Override
    public String key() {
        return (this.getClass().getSimpleName() + '.' + name());
    }

}
