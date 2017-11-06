package eu.slipo.workbench.web.model.resource;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * An abstract data source of a dataset
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
    @Type(value = UploadDataSource.class, name = "UPLOAD"),
    @Type(value = FileSystemDataSource.class, name = "FILESYSTEM"),
    @Type(value = ExternalUrlDataSource.class, name = "EXTERNAL_URL"),
})
public abstract class DataSource {

    @JsonDeserialize(using = EnumDataSource.Deserializer.class)
    protected EnumDataSource type;

    protected DataSource() {

    }

    protected DataSource(EnumDataSource type) {
        this.type = type;
    }

    public EnumDataSource getType() {
        return type;
    }

}
