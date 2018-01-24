package eu.slipo.workbench.web.model.resource;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.vividsolutions.jts.geom.Geometry;

import eu.slipo.workbench.web.model.EnumDataFormat;
import eu.slipo.workbench.web.model.EnumResourceType;
import eu.slipo.workbench.web.model.Query;

/**
 * Query for searching resources
 */
public class ResourceQuery extends Query {

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
    @JsonDeserialize(using = EnumDataFormat.Deserializer.class)
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

}
