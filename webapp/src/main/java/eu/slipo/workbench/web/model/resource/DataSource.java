package eu.slipo.workbench.web.model.resource;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * An abstract data source
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
    @Type(value = UploadDataSource.class, name = "UPLOAD"),
    @Type(value = FileSystemDataSource.class, name = "FILESYSTEM"),
    @Type(value = ExternalUrlDataSource.class, name = "EXTERNAL_URL"),
    @Type(value = HarvesterDataSource.class, name = "HARVESTER"),
})
public abstract class DataSource {

    @JsonDeserialize(using = EnumDataSource.Deserializer.class)
    protected EnumDataSource type;

    protected DataSource() {

    }

    /**
     * Creates a new {@link DataSource} instance
     *
     * @param type the data source type
     */
    protected DataSource(EnumDataSource type) {
        this.type = type;
    }

    /**
     * Data source type
     *
     * @return the type of the current data source instance
     */
    public EnumDataSource getType() {
        return type;
    }

}
