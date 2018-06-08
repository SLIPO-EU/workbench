package eu.slipo.workbench.common.model.process;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.slipo.workbench.common.model.poi.EnumResourceType;

/**
 * Process input
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "inputType")
@JsonSubTypes({
    @Type(name = "CATALOG", value = CatalogResource.class),
    @Type(name = "OUTPUT", value = ProcessOutput.class),
})
public abstract class ProcessInput implements Serializable 
{
    private static final long serialVersionUID = 1L;

    protected String key;

    @JsonDeserialize(using = EnumInputType.Deserializer.class)
    protected EnumInputType inputType;

    @JsonDeserialize(using = EnumResourceType.Deserializer.class)
    protected EnumResourceType resourceType;

    protected String name;

    protected ProcessInput() {}

    protected ProcessInput(
        String key, EnumInputType inputType, String name, EnumResourceType resourceType) 
    {
        this.key = key;
        this.inputType = inputType;
        this.name = name;
        this.resourceType = resourceType;
    }
    
    protected ProcessInput(String key, EnumInputType inputType, String name) 
    {
        this.key = key;
        this.inputType = inputType;
        this.name = name;
        this.resourceType = EnumResourceType.POI_DATA;
    }

    @JsonIgnore
    public String key()
    {
        return key;
    }
    
    @JsonProperty
    public String getKey() {
        return key;
    }

    @JsonProperty
    public EnumInputType getInputType() {
        return inputType;
    }
    
    @JsonProperty
    public EnumResourceType getResourceType() {
        return resourceType;
    }

    @JsonProperty
    public void setResourceType(EnumResourceType resourceType)
    {
        this.resourceType = resourceType;
    }
    
    @JsonProperty
    public String getName() {
        return name;
    }

}
