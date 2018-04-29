package eu.slipo.workbench.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.slipo.workbench.common.model.process.ProcessDefinitionBuilderFactory;
import eu.slipo.workbench.common.service.util.ClonerService;

@Configuration
public class ProcessDefinitionBuilderFactoryConfiguration
{
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ClonerService cloner;
    
    @Bean
    public ProcessDefinitionBuilderFactory processDefinitionBuilderFactory()
    {
        return new ProcessDefinitionBuilderFactory(objectMapper, cloner);
    }
}
