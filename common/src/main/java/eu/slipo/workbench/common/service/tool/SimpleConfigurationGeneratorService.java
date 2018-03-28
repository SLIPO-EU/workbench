package eu.slipo.workbench.common.service.tool;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import eu.slipo.workbench.common.model.tool.EnumConfigurationFormat;
import eu.slipo.workbench.common.service.util.PropertiesConverterService;

@Service
public class SimpleConfigurationGeneratorService implements ConfigurationGeneratorService
{
    @Autowired
    private ObjectMapper jsonMapper;
    
    @Autowired
    private XmlMapper xmlMapper;
    
    @Autowired
    private PropertiesConverterService propertiesConverterService;
    
    @Override
    public String generate(Object source, EnumConfigurationFormat configFormat)
        throws IOException, UnsupportedOperationException
    {
        if (source instanceof Properties) {
            // The source is already given as a map of properties
            return generateFromProperties((Properties) source); 
        } else {
            // The source is tool-specific configuration bean
            String text = null;
            switch (configFormat) {
            case JSON:
                text = jsonMapper.writeValueAsString(source);
                break;
            case XML:
                {
                    // Todo Support generation of XML configuration
                }
                break;
            default:
            case PROPERTIES:
                text = generateFromProperties(propertiesConverterService.valueToProperties(source));
                break;
            }
            return text;
        }
    }

    private String generateFromProperties(Properties properties) throws IOException
    {
        String text = null;
        try (StringWriter writer = new StringWriter()) {
            properties.store(writer, "This configuration is auto-generated");
            text = writer.toString();
        }
        return text;
    }   
}
