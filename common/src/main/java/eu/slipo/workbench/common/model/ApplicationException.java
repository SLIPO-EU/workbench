package eu.slipo.workbench.common.model;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.util.Assert;

public class ApplicationException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    /**
     * An error code that generally categorized this application-level exception.
     */
    private final ErrorCode code;

    /**
     * A message pattern for this exception (to be used when formatting with context variables)
     */
    private String pattern;

    /**
     * A list of variables holding the context of this exception
     */
    private List<Object> vars;

    /**
     * A pre-formatted message for this exception
     */
    private String message;

    private ApplicationException(Throwable cause, ErrorCode code)
    {
        super(cause);
        this.code = code;
    }

    public static ApplicationException fromMessage(String message)
    {
        return fromMessage(null, BasicErrorCode.UNKNOWN, message);
    }

    public static ApplicationException fromMessage(ErrorCode code, String message)
    {
        return fromMessage(null, code, message);
    }

    public static ApplicationException fromMessage(Throwable cause, String message)
    {
        return fromMessage(cause, BasicErrorCode.UNKNOWN, message);
    }

    public static ApplicationException fromMessage(Throwable cause, ErrorCode code, String message)
    {
        ApplicationException e = new ApplicationException(cause, code);
        Assert.notNull(message, "Expected a non-null message");
        e.message = message;
        e.pattern = null;
        return e;
    }

    public static ApplicationException fromPattern(String pattern)
    {
        return fromPattern(null, BasicErrorCode.UNKNOWN, pattern, Collections.emptyList());
    }

    public static ApplicationException fromPattern(ErrorCode code, String pattern)
    {
        return fromPattern(null, code, pattern, Collections.emptyList());
    }

    public static ApplicationException fromPattern(ErrorCode code)
    {
        return fromPattern(null, code, null, Collections.emptyList());
    }

    public static ApplicationException fromPattern(ErrorCode code, String pattern, List<?> vars)
    {
        return fromPattern(null, code, pattern, vars);
    }

    public static ApplicationException fromPattern(ErrorCode code, List<?> vars)
    {
        return fromPattern(null, code, null, vars);
    }

    public static ApplicationException fromPattern(Throwable cause, String pattern)
    {
        return fromPattern(cause, BasicErrorCode.UNKNOWN, pattern, Collections.emptyList());
    }

    public static ApplicationException fromPattern(Throwable cause, ErrorCode code, String pattern)
    {
        return fromPattern(cause, code, pattern, Collections.emptyList());
    }

    public static ApplicationException fromPattern(Throwable cause, ErrorCode code)
    {
        return fromPattern(cause, code, null, Collections.emptyList());
    }

    public static ApplicationException fromPattern(Throwable cause, ErrorCode code, List<?> vars)
    {
        return fromPattern(cause, code, null, vars);
    }

    public static ApplicationException fromPattern(Throwable cause, ErrorCode code, String pattern, List<?> vars)
    {
        ApplicationException e = new ApplicationException(cause, code);
        Assert.isTrue(pattern != null || (code != null && !code.key().isEmpty()),
            "Expected a non-null pattern or a non-empty error code");
        e.message = null;
        e.pattern = pattern;
        e.vars = new ArrayList<>(vars);
        return e;
    }

    public ErrorCode getErrorCode()
    {
        return code;
    }

    public List<Object> getContextVars()
    {
        return Collections.unmodifiableList(vars);
    }

    @Override
    public String getMessage()
    {
        if (message != null) {
            // Assume a pre-formatted message is present, return as-is
            return message;
        } else if (pattern != null) {
            // Format a message using pattern and context variables
            return MessageFormat.format(pattern, vars.toArray());
        } else {
            // Cannot format without a MessageSource, so just return key
            return code.key();
        }
    }

    /**
     * Return an instance of an {@link ApplicationException} (of same error code)
     * by formatting the message with the assist of a given {@link MessageSource}.
     *
     * <p>
     * Note: In case <tt>this</tt> is already formatted, it is returned as-is. Otherwise,
     * a new instance is created and returned. In any case, <tt>this</tt> remains unchanged.
     *
     * @param messageSource
     * @param locale
     * @return
     */
    public ApplicationException withFormattedMessage(MessageSource messageSource, Locale locale)
    {
        if (message == null && pattern == null) {
            // Resolve pattern by code; create new exception with formatted message
            String message = messageSource.getMessage(code.key(), vars.toArray(), locale);
            return fromMessage(this.getCause(), code, message);
        } else {
            // Either a message or an pattern is present: no need to use MessageSource
            return this;
        }
    }

    /**
     * Return an instance of an {@link Error} (of same error code)
     * by formatting the message with the assist of a given {@link MessageSource}.
     *
     * @param messageSource
     * @param locale
     * @return
     */
    public Error toError(MessageSource messageSource, Locale locale) {
        return new Error(code, withFormattedMessage(messageSource, locale).getMessage());
    }

}
