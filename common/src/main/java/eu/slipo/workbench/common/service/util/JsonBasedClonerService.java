package eu.slipo.workbench.common.service.util;

import java.io.IOException;
import java.io.Serializable;

import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonBasedClonerService implements ClonerService
{
    private final ObjectMapper objectMapper;
    
    public JsonBasedClonerService(ObjectMapper objectMapper)
    {
        Assert.notNull(objectMapper, "An object mapper is required");
        this.objectMapper = objectMapper;
    }

    @Override
    public Object cloneAsBean(Object source) throws IOException
    {   
        Assert.notNull(source, "A source object is required");
        
        byte[] sourceData = objectMapper.writeValueAsBytes(source);
        return objectMapper.readValue(sourceData, source.getClass());
    }
    
    @Override
    public <T> T cloneAsBean(Object source, Class<T> targetType) throws IOException
    {
        Assert.notNull(source, "A source object is required");
        Assert.notNull(targetType, "A target type is required");
        
        byte[] sourceData = objectMapper.writeValueAsBytes(source);
        return objectMapper.readValue(sourceData, targetType);
    }
}
