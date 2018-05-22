package eu.slipo.workbench.common.service.util;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;

import org.springframework.core.io.Resource;

/**
 * A service interface converting properties ({@link Properties}) to/from beans. 
 */
public interface PropertiesConverterService
{
    /**
     * A checked exception thrown to indicate that a requested conversion has failed.
     */
    @SuppressWarnings("serial")
    public class ConversionFailedException extends Exception 
    {
        public ConversionFailedException(String message, Throwable cause)
        {
            super(message, cause);
        }
    }
    
    /**
     * Convert a bean to a map of properties
     *  
     * @param value The bean to be converted
     */
    Properties valueToProperties(Object value);

    /**
     * Create a bean from a map of properties.
     * 
     * @param props The given map of properties
     * @param valueType The target type
     * @throws ConversionFailedException
     */
    <T extends Serializable> T propertiesToValue(Properties props, Class<T> valueType)
        throws ConversionFailedException;
    
    /**
     * Load properties from resource and convert to a bean
     * 
     * @param resource The resource to read properties from
     * @param valueType The target type
     * @return
     * @throws ConversionFailedException
     */
    <T extends Serializable> T propertiesToValue(Resource resource, Class<T> valueType)
        throws ConversionFailedException, IOException;
    
    /**
     * Create a bean from a map of properties.
     * <p>
     * Note that this method will not examine nested maps/arrays into given map, i.e. it will
     * consider the map as a flat map of properties (see {@link Properties}). If nested structure
     * should be examined, then an ordinary JSON deserialization is preferable.  
     * 
     * @param map A map with property-like keys (e.g. "foo.baz") mapping to arbitrary values. 
     * @param valueType The target type
     * @throws ConversionFailedException
     */
    <T extends Serializable> T propertiesToValue(Map<String, ?> map, Class<T> valueType)
        throws ConversionFailedException;

    /**
     * Create a bean from a map of properties lying under a certain root property.
     * 
     * @param props The given map of properties
     * @param rootPropertyName The name of the root property (without the trailing dot) for 
     *   properties we are interested into.  
     * @param valueType The target type
     * @throws ConversionFailedException
     */
    <T extends Serializable> T propertiesToValue(
            Properties props, String rootPropertyName, Class<T> valueType) 
        throws ConversionFailedException;

    /**
     * Load properties from resource and convert to a bean
     * 
     * @param resource The resource to read properties from
     * @param rootPropertyName The name of the root property (without the trailing dot) for
     *   properties we are interested into.
     * @param valueType The target type
     * @return
     * @throws ConversionFailedException
     */
    <T extends Serializable> T propertiesToValue(
        Resource resource, String rootPropertyName, Class<T> valueType) 
    throws ConversionFailedException, IOException;
    
    /**
     * @see PropertiesConverterService#propertiesToValue(Properties, String, Class)
     */
    <T extends Serializable> T propertiesToValue(
            Map<String, Object> map, String rootPropertyName, Class<T> valueType) 
        throws ConversionFailedException;

    /**
     * @see PropertiesConverterService#propertiesToValue(Properties, String, Class)
     */
    <T extends Serializable> T propertiesToValue(
            SortedMap<String, Object> map, String rootPropertyName, Class<T> valueType) 
        throws ConversionFailedException;
}
