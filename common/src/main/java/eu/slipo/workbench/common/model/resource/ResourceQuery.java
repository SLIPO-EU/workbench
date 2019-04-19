package eu.slipo.workbench.common.model.resource;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.vividsolutions.jts.geom.Geometry;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumResourceType;

/**
 * Query for searching resources
 */
public class ResourceQuery {

    /**
     * Search resources by name using LIKE SQL operator
     */
    private String name;

    /**
     * Search resources by description using LIKE SQL operator
     */
    private String description;

    /**
     * Search resources by the initial data format
     */
    private EnumDataFormat format;

    /**
     * Search resources by resource data type
     */
    @JsonDeserialize(using = EnumResourceType.Deserializer.class)
    private EnumResourceType type;

    /**
     * Search resources by checking bounding box intersection
     */
    private Geometry boundingBox;

    /**
     * Search for resources with more than {@code size} entities
     */
    private Integer size;

    /**
     * Search by the ID of the user that created this resource
     */
    private Integer createdBy;

    /**
     * Include only processes that have been successfully exported to PostGIS
     */
    private boolean exported;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EnumDataFormat getFormat() {
        if (format == null) {
            return EnumDataFormat.UNDEFINED;
        }
        return format;
    }

    public void setFormat(EnumDataFormat format) {
        this.format = format;
    }

    public EnumResourceType getType() {
        if (type == null) {
            return EnumResourceType.UNDEFINED;
        }
        return type;
    }

    public void setType(EnumResourceType type) {
        this.type = type;
    }

    public Geometry getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(Geometry boundingBox) {
        this.boundingBox = boundingBox;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public boolean isExported() {
        return exported;
    }

    public void setExported(boolean exported) {
        this.exported = exported;
    }

}
