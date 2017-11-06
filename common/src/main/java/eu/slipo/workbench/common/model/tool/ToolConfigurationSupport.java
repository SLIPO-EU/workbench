package eu.slipo.workbench.common.model.tool;

import java.util.Map;
import java.util.Properties;

import org.apache.commons.beanutils.BeanUtils;
import com.fasterxml.jackson.core.JsonProcessingException;

import eu.slipo.workbench.common.util.BeanToPropertiesConverter;

/**
 * An abstract class providing support for common copy/serialize/deserialize functionality.
 */
@SuppressWarnings("serial")
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
     * Create a configuration bean from a map of properties.
     * 
     * @param props The map of properties
     * @param resultType The class of the result object
     * 
     * @throws JsonProcessingException if underlying JSON deserialization fails 
     */
    public static <B extends ToolConfigurationSupport> B fromProperties(
            Properties props, Class<B> resultType) 
        throws JsonProcessingException
    {
        return BeanToPropertiesConverter.propertiesToValue(props, resultType);
    }
    
    /**
     * Create a configuration bean from a map of properties.
     * 
     * @param props
     * @param rootPropertyName The name of the root property (without the trailing dot) to search under
     * @param resultType
     * @throws JsonProcessingException
     */
    public static <B extends ToolConfigurationSupport> B fromProperties(
            Properties props, String rootPropertyName, Class<B> resultType) 
        throws JsonProcessingException
    {
        return BeanToPropertiesConverter.propertiesToValue(props, rootPropertyName, resultType);
    }
    
    /**
     * Create a configuration bean from a map of properties.
     * @see ToolConfigurationSupport#fromProperties(Properties, Class)
     * 
     * @param map The map of properties
     * @param resultType The class of the result object
     * @throws JsonProcessingException
     */
    public static <B extends ToolConfigurationSupport> B fromProperties(
            Map<String,Object> map, Class<B> resultType) 
        throws JsonProcessingException
    {
        return BeanToPropertiesConverter.propertiesToValue(map, resultType);
    }
    
    /**
     * @see ToolConfigurationSupport#fromProperties(Properties, String, Class)
     */
    public static <B extends ToolConfigurationSupport> B fromProperties(
        Map<String,Object> map, String rootPropertyName, Class<B> resultType) 
    throws JsonProcessingException
    {
        return BeanToPropertiesConverter.propertiesToValue(map, rootPropertyName, resultType);
    }
    
    /**
     * Clone this bean in a field-wise manner. All fields are shallow copies using reflective 
     * getter/setter methods.  
     * 
     * @throws ReflectiveOperationException if a method/constructor (called reflectively) fails
     */
    public ToolConfigurationSupport cloneAsBean() 
        throws ReflectiveOperationException
    {
        return (ToolConfigurationSupport) BeanUtils.cloneBean(this);
    }
    
    /**
     * Clone this bean in a field-wise manner, and cast to a target type. All fields are shallow copies 
     * using reflective getter/setter methods.
     * 
     * @param resultType The type to cast the cloned object 
     * @throws ReflectiveOperationException if a method/constructor (called reflectively) fails, or if
     *    the desired cast fails.
     */
    public <B extends ToolConfigurationSupport> B cloneAsBean(Class<B> resultType) 
        throws ReflectiveOperationException
    {
        return resultType.cast(BeanUtils.cloneBean(this));
    }
    
    //
    // Todo Provide support methods for XML serialization/deserialization
    //
}
