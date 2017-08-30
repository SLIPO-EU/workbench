package eu.slipo.workbench.common.model;

public enum BasicErrorCode implements ErrorCode
{
    UNKNOWN,
    NOT_IMPLEMENTED,
    
    PARSE_ERROR,
    
    RESOURCE_NOT_FOUND,
    LOCALE_NOT_SUPPORTED,
    
    NO_RESULT,
    
    REST_RESPONSE_WITH_ERRORS,  // REST response flags application-side errors   
    REST_TIMEOUT,               // REST client reached timeout (on reading)
    REST_CLIENT_EXCEPTION,      // REST client encountered HTTP-side problems (e.g. connectivity)
    
    AUTHENTICATION_FAILED,
    AUTHENTICATION_REQUIRED,
    ;

    @Override
    public String key()
    {
        return (this.getClass().getSimpleName() + '.' + name());
    }

}
