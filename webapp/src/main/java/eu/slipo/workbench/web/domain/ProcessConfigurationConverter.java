package eu.slipo.workbench.web.domain;

import java.io.IOException;

import javax.persistence.AttributeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.slipo.workbench.web.model.process.ProcessDefinitionUpdate;

@Component
public class ProcessConfigurationConverter implements AttributeConverter<ProcessDefinitionUpdate, String> {

    private static final Logger logger = LoggerFactory.getLogger(ProcessConfigurationConverter.class);

    private static ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        ProcessConfigurationConverter.objectMapper = objectMapper;
    }

    @Override
    public String convertToDatabaseColumn(ProcessDefinitionUpdate attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            logger.error("Failed to write process configuration", e);
        }
        return null;
    }

    @Override
    public ProcessDefinitionUpdate convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, new TypeReference<ProcessDefinitionUpdate>() { });
        } catch (IOException e) {
            logger.error("Failed to read process configuration", e);
        }
        return null;
    }

}
