package eu.slipo.workbench.common.model.process;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.slipo.workbench.common.model.poi.EnumResourceType;

/**
 * Process input
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "inputType")
@JsonSubTypes({
    @Type(value = CatalogResource.class, name = "CATALOG"),
    @Type(value = ProcessOutput.class, name = "OUTPUT"),
})
public abstract class ProcessInput {

    protected int key;

    @JsonDeserialize(using = EnumInputType.Deserializer.class)
    protected EnumInputType inputType;

    @JsonDeserialize(using = EnumResourceType.Deserializer.class)
    protected EnumResourceType resourceType;

    private String name;

    protected ProcessInput() {}

    protected ProcessInput(
        int key, EnumInputType inputType, EnumResourceType resourceType, String name) 
    {
        this.key = key;
        this.inputType = inputType;
        this.resourceType = resourceType;
        this.name = name;
    }

    @JsonProperty
    public int getKey() {
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
    public String getName() {
        return name;
    }

}
