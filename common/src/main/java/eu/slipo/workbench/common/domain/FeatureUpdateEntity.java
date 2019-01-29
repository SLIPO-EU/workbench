package eu.slipo.workbench.common.domain;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.databind.JsonNode;
import com.vividsolutions.jts.geom.Geometry;

import eu.slipo.workbench.common.model.poi.FeatureUpdateRecord;

@Entity(name = "FeatureUpdate")
@Table(
    schema = "public",
    name = "feature_update_history"
)
public class FeatureUpdateEntity {

    @Id
    @Column(name = "id", updatable = false)
    @SequenceGenerator(
        sequenceName = "feature_update_history_seq", name = "feature_update_history_seq", initialValue = 1, allocationSize = 1)
    @GeneratedValue(generator = "feature_update_history_seq", strategy = GenerationType.SEQUENCE)
    long id = -1L;

    @Column(name = "table_name", columnDefinition = "uuid")
    UUID tableName;

    @NotBlank
    @Column(name = "`feature_id`", nullable = false, updatable = false)
    String featureId;

    @Column(name = "properties", updatable = false, nullable = false)
    JsonNode properties;

    @Column(name = "the_geom")
    Geometry geometry;

    @NotNull
    @Column(name = "updated_on", nullable = false, updatable = false)
    ZonedDateTime updatedOn;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "updated_by", nullable = false, updatable = false)
    AccountEntity updatedBy;

    public long getId() {
        return id;
    }

    public UUID getTableName() {
        return tableName;
    }

    public String getFeatureId() {
        return featureId;
    }

    public JsonNode getProperties() {
        return properties;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public ZonedDateTime getUpdatedOn() {
        return updatedOn;
    }

    public AccountEntity getUpdatedBy() {
        return updatedBy;
    }

    public FeatureUpdateRecord toRecord() {
        FeatureUpdateRecord record = new FeatureUpdateRecord();
        record.setGeometry(this.geometry);
        record.setProperties(this.properties);
        record.setUpdatedBy(this.updatedBy.toAccountInfo());
        record.setUpdatedOn(this.updatedOn);
        return record;
    }

}
