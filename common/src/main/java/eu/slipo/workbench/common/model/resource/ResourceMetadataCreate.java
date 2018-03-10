package eu.slipo.workbench.common.model.resource;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;


public class ResourceMetadataCreate implements Serializable 
{
    private static final long serialVersionUID = 1L;

    private String name;

    private String description;

    protected ResourceMetadataCreate() {}

    public ResourceMetadataCreate(String name, String description) 
    {
        this.name = name;
        this.description = description;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public String getDescription() {
        return description;
    }

    @Override
    public String toString()
    {
        return String.format("ResourceMetadataCreate [%s]", name);
    }
}
