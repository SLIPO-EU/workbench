package eu.slipo.workbench.common.model.security;

import eu.slipo.workbench.common.model.ErrorCode;

public enum ApplicationKeyErrorCode implements ErrorCode
{
    UNKNOWN,
    MISSING_KEY,
    UNREGISTERED_KEY,
    REVOKED_KEY,
    ;

    @Override
    public String key()
    {
        return (this.getClass().getSimpleName() + '.' + name());
    }

}
