package eu.slipo.workbench.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.slipo.workbench.common.service.util.ClonerService;
import eu.slipo.workbench.common.service.util.JsonBasedClonerService;

@Configuration
public class ClonerServiceConfiguration
{
    @Autowired
    private ObjectMapper objectMapper;
    
    @Primary
    @Bean({ "clonerService", "jsonBasedClonerService" })
    public ClonerService clonerService()
    {
        return new JsonBasedClonerService(objectMapper);
    }
}
