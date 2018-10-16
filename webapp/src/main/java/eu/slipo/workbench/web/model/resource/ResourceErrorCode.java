package eu.slipo.workbench.web.model.resource;

import eu.slipo.workbench.common.model.ErrorCode;

/**
 * Error codes for resource operations
 */
public enum ResourceErrorCode implements ErrorCode {
    UNKNOWN,
    DATASOURCE_NOT_SUPPORTED,
    FILE_NOT_FOUND,
    FORMAT_NOT_SUPPORTED,
    NAME_DUPLICATE,
    QUERY_IS_EMPTY,
    RESOURCE_IS_EMPTY,
    RESOURCE_NOT_FOUND,
    ;

    @Override
    public String key() {
        return (this.getClass().getSimpleName() + '.' + name());
    }

}
