package eu.slipo.workbench.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.slipo.workbench.common.model.process.ProcessDefinitionBuilder;

@Configuration
public class ProcessDefinitionBuilderFactory
{
    @Autowired
    private ObjectMapper objectMapper;
   
    ProcessDefinitionBuilderFactory() {}
    
    public ProcessDefinitionBuilderFactory(ObjectMapper objectMapper)
    {
        Assert.notNull(objectMapper, "An object mapper is required");
        this.objectMapper = objectMapper;
    }
    
    public ProcessDefinitionBuilder create(String name)
    {
        return new ProcessDefinitionBuilder(name, objectMapper);
    }
}
