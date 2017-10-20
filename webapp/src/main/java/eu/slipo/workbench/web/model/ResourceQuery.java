package eu.slipo.workbench.web.model;

import com.vividsolutions.jts.geom.Geometry;

public class ResourceQuery extends Query {

    private String name;

    private Geometry boundingBox;

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
