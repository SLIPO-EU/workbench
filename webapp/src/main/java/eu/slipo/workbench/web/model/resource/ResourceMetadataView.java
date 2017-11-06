package eu.slipo.workbench.web.model.resource;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Resource metadata view model
 */
public class ResourceMetadataView {

    private String name;

    private String description;

    private Geometry boundingBox;

    private Integer size;

    public ResourceMetadataView(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public ResourceMetadataView(
            String name,
            String description,
            int size) {

        this.name = name;
        this.description = description;
        this.size = size;
    }

    public ResourceMetadataView(
            String name,
            String description,
            int size,
            Geometry boundingBox) {

        this.name = name;
        this.description = description;
        this.boundingBox = boundingBox;
        this.size = size;
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

}
