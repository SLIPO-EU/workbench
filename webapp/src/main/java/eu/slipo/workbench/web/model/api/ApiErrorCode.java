package eu.slipo.workbench.web.model.api;

import eu.slipo.workbench.common.model.ErrorCode;

/**
 * API error codes
 */
public enum ApiErrorCode implements ErrorCode
{
    UNKNOWN,
    PROFILE_NOT_FOUND,
    RESOURCE_NOT_FOUND,
    ;

    @Override
    public String key() {
        return (this.getClass().getSimpleName() + '.' + name());
    }

}
