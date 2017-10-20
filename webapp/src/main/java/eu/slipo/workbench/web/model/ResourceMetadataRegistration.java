package eu.slipo.workbench.web.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class ResourceMetadataRegistration {

    private String name;

    private String description;

    @JsonDeserialize(using = EnumDataFormat.Deserializer.class)
    private EnumDataFormat format;

    protected ResourceMetadataRegistration() {

    }

    public ResourceMetadataRegistration(
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
