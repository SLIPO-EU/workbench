package eu.slipo.workbench.common.model.resource;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;

/**
 * Model resource metadata
 */
public class ResourceMetadataCreate implements Serializable 
{
    private static final long serialVersionUID = 1L;

    private String name;

    private String description;

    private EnumDataFormat format;

    protected ResourceMetadataCreate() {}

    public ResourceMetadataCreate(String name, String description, EnumDataFormat format) 
    {
        this.name = name;
        this.description = description;
        this.format = format;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public String getDescription() {
        return description;
    }

    @JsonProperty
    public EnumDataFormat getFormat() {
        return format;
    }
}
