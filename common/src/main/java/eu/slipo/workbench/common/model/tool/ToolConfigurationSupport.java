package eu.slipo.workbench.common.model.tool;

import java.util.Properties;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.slipo.workbench.common.util.BeanToPropertiesConverter;

/**
 * An abstract class providing support for common serialize/deserialize functionality.
 */
public abstract class ToolConfigurationSupport implements ToolConfiguration
{
    /**
     * Convert this object to a map of properties ({@link Properties}).
     */
    public Properties toProperties()
    {
        return BeanToPropertiesConverter.valueToProperties(this);
    }
    
    /**
     * Create a configuration object from a map of properties.
     * 
     * @param props The map of properties
     * @param cls The class of the target object
     * 
     * @throws JsonProcessingException if underlying JSON deserialization fails 
     */
    public static <T extends AbstractToolConfiguration> T fromProperties(Properties props, Class<T> cls) 
        throws JsonProcessingException
    {
        return BeanToPropertiesConverter.propertiesToValue(props, cls);
    }
}
