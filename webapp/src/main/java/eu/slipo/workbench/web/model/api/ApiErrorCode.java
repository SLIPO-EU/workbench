package eu.slipo.workbench.web.model.api;

import eu.slipo.workbench.common.model.ErrorCode;

/**
 * API error codes
 */
public enum ApiErrorCode implements ErrorCode
{
    UNKNOWN,
    PROFILE_NOT_FOUND,
    EMPTY_PATH,
    RELATIVE_PATH_REQUIRED,
    FILE_NOT_FOUND,
    RESOURCE_NOT_FOUND,
    OUTPUT_FILE_NOT_FOUND,
    FILE_TYPE_NOT_SUPPORTED,
    ;

    @Override
    public String key() {
        return (this.getClass().getSimpleName() + '.' + name());
    }

}
