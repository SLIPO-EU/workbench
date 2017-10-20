package eu.slipo.workbench.web.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.vividsolutions.jts.geom.Geometry;

public class ResourceMetadata {

    private String name;

    private String description;

    private Geometry boundingBox;

    private Integer size;

    @JsonDeserialize(using = EnumDataFormat.Deserializer.class)
    private EnumDataFormat sourceFormat;

    @JsonDeserialize(using = EnumDataFormat.Deserializer.class)
    private EnumDataFormat targetFormat;

    public ResourceMetadata(
            String name,
            String description,
            EnumDataFormat sourceFormat,
            EnumDataFormat targetFormat) {

        this.name = name;
        this.description = description;
        this.sourceFormat = sourceFormat;
        this.targetFormat = targetFormat;
    }

    public ResourceMetadata(
            String name,
            String description,
            int size,
            EnumDataFormat sourceFormat,
            EnumDataFormat targetFormat) {

        this.name = name;
        this.description = description;
        this.size = size;
        this.sourceFormat = sourceFormat;
        this.targetFormat = targetFormat;
    }

    public ResourceMetadata(
            String name,
            String description,
            int size,
            Geometry boundingBox,
            EnumDataFormat sourceFormat,
            EnumDataFormat targetFormat) {

        this.name = name;
        this.description = description;
        this.boundingBox = boundingBox;
        this.size = size;
        this.sourceFormat = sourceFormat;
        this.targetFormat = targetFormat;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Geometry getBoundingBox() {
        return boundingBox;
    }

    public Integer getSize() {
        return size;
    }

    public EnumDataFormat getSourceFormat() {
        return sourceFormat;
    }

    public void setSourceFormat(EnumDataFormat sourceFormat) {
        this.sourceFormat = sourceFormat;
    }

    public EnumDataFormat getTargetFormat() {
        return targetFormat;
    }

    public void setTargetFormat(EnumDataFormat targetFormat) {
        this.targetFormat = targetFormat;
    }

}
