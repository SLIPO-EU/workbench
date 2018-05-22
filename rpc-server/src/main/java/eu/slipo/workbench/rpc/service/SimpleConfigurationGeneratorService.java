package eu.slipo.workbench.rpc.service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import eu.slipo.workbench.common.model.tool.EnumConfigurationFormat;
import eu.slipo.workbench.common.model.tool.serialization.DtdDeclaration;
import eu.slipo.workbench.common.service.util.PropertiesConverterService;

@Service
public class SimpleConfigurationGeneratorService implements ConfigurationGeneratorService
{
    @Autowired
    private ObjectMapper jsonMapper;

    @Autowired
    private XmlMapper xmlMapper;

    @Autowired
    private PropertiesConverterService propertiesConverter;

    private ObjectWriter jsonWriter;

    private ObjectWriter xmlWriter;

    @PostConstruct
    private void setup()
    {
        this.jsonWriter = jsonMapper.writerWithDefaultPrettyPrinter();
        this.xmlWriter = xmlMapper.writerWithDefaultPrettyPrinter();
    }

    private static final String XML_DECLARATION =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";

    @Override
    public String generate(Object source, EnumConfigurationFormat configFormat)
        throws IOException, UnsupportedOperationException
    {
        Assert.notNull(source, "A source object is required");
        Assert.notNull(configFormat, "A configuration format is required");

        if (source instanceof Properties) {
            // The source is already given as a map of properties
            return generateFromProperties((Properties) source);
        } else {
            // The source is tool-specific configuration bean

            String text = null;
            Class<?> sourceType = source.getClass();

            switch (configFormat) {
            case JSON:
                {
                    text = jsonWriter.writeValueAsString(source);
                }
                break;
            case XML:
                {
                    StringBuilder textBuilder = new StringBuilder(XML_DECLARATION).append("\n");

                    DtdDeclaration dtdDeclaration = sourceType.getAnnotation(DtdDeclaration.class);
                    if (dtdDeclaration != null) {
                        textBuilder.append(String.format("<!DOCTYPE %s SYSTEM \"%s\">",
                            dtdDeclaration.name(), dtdDeclaration.href()));
                        textBuilder.append("\n");
                    }

                    textBuilder.append(xmlWriter.writeValueAsString(source));
                    text = textBuilder.toString();
                }
                break;
            default:
            case PROPERTIES:
                {
                    text = generateFromProperties(propertiesConverter.valueToProperties(source));
                }
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
