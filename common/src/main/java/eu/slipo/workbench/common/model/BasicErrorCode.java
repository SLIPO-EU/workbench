package eu.slipo.workbench.common.model;

public enum BasicErrorCode implements ErrorCode
{
    UNKNOWN,
    NOT_IMPLEMENTED,
    
    PARSE_ERROR,
    
    RESOURCE_NOT_FOUND,
    LOCALE_NOT_SUPPORTED,
    
    NO_RESULT,
    
    REST_ERROR_RESULT,
    REST_TIMEOUT,
    
    AUTHENTICATION_FAILED,
    AUTHENTICATION_REQUIRED,
    ;

    @Override
    public String key()
    {
        return (this.getClass().getSimpleName() + '.' + name());
    }

}
