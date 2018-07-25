package eu.slipo.workbench.web.model.admin;

import eu.slipo.workbench.common.model.ErrorCode;

public enum AccountErrorCode implements ErrorCode
{
    UNKNOWN,
    ACCOUNT_NOT_SET,
    ACCOUNT_NOT_FOUND,
    INVALID_FAMILY_NAME,
    INVALID_GIVEN_NAME,
    NO_ROLE_SET,
    ;

    @Override
    public String key()
    {
        return (this.getClass().getSimpleName() + '.' + name());
    }

}
