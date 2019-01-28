package eu.slipo.workbench.common.model.poi;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.databind.JsonNode;
import com.vividsolutions.jts.geom.Geometry;

import eu.slipo.workbench.common.model.user.AccountInfo;

public class FeatureUpdateRecord {

    private JsonNode properties;
    private Geometry geometry;
    private ZonedDateTime updatedOn;
    private AccountInfo updatedBy;

    public JsonNode getProperties() {
        return properties;
    }

    public void setProperties(JsonNode properties) {
        this.properties = properties;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public ZonedDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(ZonedDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }

    public AccountInfo getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(AccountInfo updatedBy) {
        this.updatedBy = updatedBy;
    }

}
