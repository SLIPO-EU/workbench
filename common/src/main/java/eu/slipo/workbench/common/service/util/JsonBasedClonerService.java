package eu.slipo.workbench.common.service.util;

import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonBasedClonerService implements ClonerService
{
    private final ObjectMapper objectMapper;
    
    public JsonBasedClonerService(ObjectMapper objectMapper)
    {
        Validate.notNull(objectMapper, "An object mapper is required");
        this.objectMapper = objectMapper;
    }

    @Override
    public <B extends Serializable> B cloneAsBean(B source) throws IOException
    {   
        byte[] sourceData = objectMapper.writeValueAsBytes(source);
        Object resultObject = objectMapper.readValue(sourceData, source.getClass());
        
        @SuppressWarnings("unchecked")
        B result = (B) resultObject;
        
        return result;
    }

}
