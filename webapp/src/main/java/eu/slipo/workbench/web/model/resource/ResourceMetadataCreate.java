package eu.slipo.workbench.web.model.resource;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.slipo.workbench.web.model.EnumDataFormat;

/**
 * Resource metadata create model
 */
public class ResourceMetadataCreate {

    private String name;

    private String description;

    @JsonDeserialize(using = EnumDataFormat.Deserializer.class)
    private EnumDataFormat format;

    protected ResourceMetadataCreate() {

    }

    public ResourceMetadataCreate(
        String name,
        String description,
        EnumDataFormat format) {

        this.name = name;
        this.description = description;
        this.format = format;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public EnumDataFormat getFormat() {
        return format;
    }

}
