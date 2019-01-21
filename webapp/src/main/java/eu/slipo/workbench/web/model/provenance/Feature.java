package eu.slipo.workbench.web.model.provenance;

import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;

public class Feature {

    private Map<String, String> properties;

    private Geometry geometry;

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

}
