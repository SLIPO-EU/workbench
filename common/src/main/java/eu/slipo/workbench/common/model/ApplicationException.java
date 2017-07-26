package eu.slipo.workbench.common.model;

import java.util.Collections;
import java.util.Map;

import com.ibm.icu.text.MessageFormat;

public class ApplicationException extends RuntimeException
{
    /**
     * An error code that generally categorized this application-level exception
     */
    private final ErrorCode code;
    
    /**
     * A message pattern for this specific exception instance.
     */
    private final String pattern;
    
    /**
     * A map of variables holding the context of this exception
     */
    private final Map<String,Object> vars; 
    
    public ApplicationException(ErrorCode code, Map<String,Object> vars)
    {
        this(null, code, null, vars);
    }

    public ApplicationException(Throwable cause, ErrorCode code, Map<String,Object> vars)
    {
        this(cause, code, null, vars);
    }
    
    public ApplicationException(String pattern, Map<String,Object> vars)
    {
        this(null, BasicErrorCode.UNKNOWN, pattern, vars);
    }
    
    public ApplicationException(Throwable cause, String pattern, Map<String,Object> vars)
    {
        this(cause, BasicErrorCode.UNKNOWN, pattern, vars);
    }
    
    public ApplicationException(String message)
    {
        this(null, BasicErrorCode.UNKNOWN, message, Collections.emptyMap());
    }
    
    public ApplicationException(Throwable cause, String message)
    {
        this(cause, BasicErrorCode.UNKNOWN, message, Collections.emptyMap());
    }
    
    public ApplicationException(ErrorCode code)
    {
        this(null, code, null, Collections.emptyMap());
    }
    
    public ApplicationException(Throwable cause, ErrorCode code)
    {
        this(cause, code, null, Collections.emptyMap());
    }
    
    public ApplicationException(ErrorCode code, String message)
    {
        this(null, code, message, Collections.emptyMap());
    }
    
    public ApplicationException(Throwable cause, ErrorCode code, String message)
    {
        this(cause, code, message, Collections.emptyMap());
    }
    
    public ApplicationException(Throwable cause, ErrorCode code, String pattern, Map<String,Object> vars)
    {
        super(cause);
        
        this.code = code;
        this.pattern = pattern;
        this.vars = vars;
    }
    
    public ErrorCode getCode()
    {
        return code;
    }
    
    public Map<String,Object> getContextVars()
    {
        return Collections.unmodifiableMap(vars);
    }

    public String getMessage()
    {
        // If a message pattern is given, go ahead and format a message.
        // Otherwise (since no MessageSource is available here), simply 
        // return the message key
        
        if (pattern == null || pattern.isEmpty())
            return code.key();
        else if (vars.isEmpty())
            return pattern; // nothing to format
        else 
            return MessageFormat.format(pattern, vars);      
    }
}
