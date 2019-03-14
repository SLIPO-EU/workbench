package eu.slipo.workbench.web.model.api.resource;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vividsolutions.jts.geom.Geometry;

import eu.slipo.workbench.common.model.resource.ResourceRecord;

public class ResourceSimpleRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id = -1L;

    private long version = -1L;

    private String name;

    private String description;

    private ZonedDateTime createdOn;

    private Long size;

    private Geometry boundingBox;

    private Integer numberOfEntities;

    private List<ResourceSimpleRecord> revisions = new ArrayList<ResourceSimpleRecord>();

    public ResourceSimpleRecord() {
    }

    public ResourceSimpleRecord(ResourceRecord record) {
        this.id = record.getId();
        this.version = record.getVersion();
        this.name = record.getName();
        this.description = record.getDescription();
        this.createdOn = record.getCreatedOn();
        this.size = record.getFileSize();
        this.boundingBox = record.getBoundingBox();
        this.numberOfEntities = record.getNumberOfEntities();

        for (ResourceRecord r : record.getRevisions()) {
            if (r.getVersion() != this.version) {
                this.revisions.add(new ResourceSimpleRecord(r));
            }
        }
    }

    public long getId() {
        return id;
    }

    public long getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ZonedDateTime getCreatedOn() {
        return createdOn;
    }

    public Long getSize() {
        return size;
    }

    public Geometry getBoundingBox() {
        return boundingBox;
    }

    public Integer getNumberOfEntities() {
        return numberOfEntities;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<ResourceSimpleRecord> getRevisions() {
        return this.revisions == null ? Collections.emptyList() : Collections.unmodifiableList(this.revisions);
    }

}
