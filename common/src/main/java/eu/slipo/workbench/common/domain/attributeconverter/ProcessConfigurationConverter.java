package eu.slipo.workbench.common.domain.attributeconverter;

import java.io.IOException;

import javax.persistence.Converter;
import javax.persistence.AttributeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.slipo.workbench.common.model.process.ProcessDefinition;

@Converter
public class ProcessConfigurationConverter implements AttributeConverter<ProcessDefinition, String>
{
    private static final Logger logger = LoggerFactory.getLogger(ProcessConfigurationConverter.class);

    private static ObjectMapper objectMapper;
    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JtsModule());
    }
    
    @Override
    public String convertToDatabaseColumn(ProcessDefinition attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            logger.error("Failed to write process configuration", e);
        }
        return null;
    }

    @Override
    public ProcessDefinition convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, new TypeReference<ProcessDefinition>() { });
        } catch (IOException e) {
            logger.error("Failed to read process configuration", e);
        }
        return null;
    }

}
