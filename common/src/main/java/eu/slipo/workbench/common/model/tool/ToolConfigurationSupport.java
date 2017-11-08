package eu.slipo.workbench.common.model.tool;

import java.util.Map;
import java.util.Properties;

import org.apache.commons.beanutils.BeanUtils;

/**
 * An abstract class providing support for common copy/serialize/deserialize functionality.
 */
@SuppressWarnings("serial")
public abstract class ToolConfigurationSupport implements ToolConfiguration
{
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
}
