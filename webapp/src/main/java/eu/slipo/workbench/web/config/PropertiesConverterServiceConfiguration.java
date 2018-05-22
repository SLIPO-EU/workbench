package eu.slipo.workbench.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.slipo.workbench.common.service.util.JsonBasedPropertiesConverterService;
import eu.slipo.workbench.common.service.util.PropertiesConverterService;

@Configuration
public class PropertiesConverterServiceConfiguration
{
    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    public PropertiesConverterService propertiesConverterService()
    {
        return new JsonBasedPropertiesConverterService(objectMapper);
    }
}