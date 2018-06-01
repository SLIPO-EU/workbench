package eu.slipo.workbench.common.model.tool.output;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.springframework.util.Assert;

import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.tool.AnyTool;

public interface OutputPart <T extends AnyTool>
{
    String key();

    EnumOutputType outputType();
    
    Class<T> toolType();
    
    default EnumTool tool()
    {
        return EnumTool.fromType(toolType());
    }

    final String _KEY_PATTERN = "[a-z][-_a-z0-9]*";
    
    final String _DEFAULT_KEY = ".";
    
    /**
     * Validate a key against a common pattern
     * 
     * @param key
     * @throws IllegalArgumentException if the key is invalid
     */
    static void validateKey(String key)
    {
        if (!Pattern.matches(_KEY_PATTERN, key)) {
            throw new IllegalArgumentException("The key [" + key + "] is invalid");
        }
    }
    
    /**
     * Get a default key representing a default output part. This key should be outside the range 
     * of valid keys (as determined by {@link OutputPart#validateKey(String)}).
     * 
     * @return a key
     */
    static String defaultKey()
    {
        return _DEFAULT_KEY;
    }
    
    /**
     * Lookup for a constant with a given key
     * 
     * @param key The key to search for
     * @param enumeration The enumeration to search into
     * @return a enumeration constant, or <tt>null</tt> if no match is found
     */
    static <Y extends AnyTool, E extends Enum<E> & OutputPart<Y>> E fromKey(
        String key, Class<E> enumeration)
    {
        Assert.notNull(key, "A key is required");
        Assert.notNull(enumeration, "The enumeration class is required");

        for (E e: enumeration.getEnumConstants())
            if (key.equals(e.key()))
                return e;
        return null;
    }
}
