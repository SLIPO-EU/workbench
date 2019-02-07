package eu.slipo.workbench.web.model.provenance;

import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;

public class Feature {

    private Geometry geometry;
    private Map<String, String> properties;

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
