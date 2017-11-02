package eu.slipo.workbench.common.model.tool;

import java.util.Properties;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.util.BeanToPropertiesConverter;

/**
 * An abstract base class for tool configuration.
 */
@SuppressWarnings("serial")
public abstract class AbstractToolConfiguration extends ToolConfigurationSupport
{
    /**
     * The expected data format for input.
     */
    protected EnumDataFormat inputFormat;
    
    /**
     * The expected data format for output.
     */
    protected EnumDataFormat outputFormat;
   
}
