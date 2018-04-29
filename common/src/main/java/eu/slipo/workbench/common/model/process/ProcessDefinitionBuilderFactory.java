package eu.slipo.workbench.common.model.process;

import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.slipo.workbench.common.service.util.ClonerService;
import eu.slipo.workbench.common.service.util.JsonBasedClonerService;

public class ProcessDefinitionBuilderFactory
{
    private final ObjectMapper objectMapper;
    
    private final ClonerService cloner;
    
    public ProcessDefinitionBuilderFactory(ObjectMapper objectMapper)
    {
        this(objectMapper, new JsonBasedClonerService(objectMapper));
    }
    
    public ProcessDefinitionBuilderFactory(ObjectMapper objectMapper, ClonerService cloner)
    {
        Assert.notNull(objectMapper, "An object mapper is required");
        Assert.notNull(cloner, "A cloner service is required");
        this.objectMapper = objectMapper;
        this.cloner = cloner;
    }

    public ProcessDefinitionBuilder create(String name)
    {
        return new ProcessDefinitionBuilder(name, cloner);
    }
}
