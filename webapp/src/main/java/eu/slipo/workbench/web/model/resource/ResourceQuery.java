package eu.slipo.workbench.web.model.resource;

import com.vividsolutions.jts.geom.Geometry;

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
