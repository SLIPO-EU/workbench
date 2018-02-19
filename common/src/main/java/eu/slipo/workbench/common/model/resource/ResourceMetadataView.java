package eu.slipo.workbench.common.model.resource;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Resource metadata view model
 */
public class ResourceMetadataView {

    private String name;

    private String description;

    private Geometry boundingBox;

    private Integer size;

    public ResourceMetadataView(String name, String description) 
    {
        this(name, description, null, null);
    }

    public ResourceMetadataView(String name, String description, Integer size) 
    {
        this(name, description, size, null);
    }

    public ResourceMetadataView(
        String name, String description, Integer size, Geometry boundingBox) 
    {
        this.name = name;
        this.description = description;
        this.size = size;
        this.boundingBox = boundingBox;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Integer getSize() {
        return size;
    }

    public Geometry getBoundingBox() {
        return boundingBox;
    }

}
