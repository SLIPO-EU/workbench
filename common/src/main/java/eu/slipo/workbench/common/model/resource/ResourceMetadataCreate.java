package eu.slipo.workbench.common.model.resource;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;

/**
 * Resource metadata create model
 */
public class ResourceMetadataCreate {

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
