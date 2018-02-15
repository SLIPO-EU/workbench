package eu.slipo.workbench.common.model.resource;

import java.io.Serializable;

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
public abstract class DataSource implements Serializable 
{
    private static final long serialVersionUID = 1L;
    
    @JsonDeserialize(using = EnumDataSourceType.Deserializer.class)
    protected EnumDataSourceType type;

    protected DataSource() {}

    /**
     * Creates a new {@link DataSource} instance
     *
     * @param type the data source type
     */
    protected DataSource(EnumDataSourceType type) {
        this.type = type;
    }

    /**
     * Data source type
     *
     * @return the type of the current data source instance
     */
    public EnumDataSourceType getType() {
        return type;
    }
}
