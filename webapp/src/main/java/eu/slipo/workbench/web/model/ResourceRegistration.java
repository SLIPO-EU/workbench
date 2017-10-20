package eu.slipo.workbench.web.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "source")
@JsonSubTypes({
    @Type(value = UploadResourceRegistration.class, name = "UPLOAD"),
    @Type(value = FileSystemResourceRegistration.class, name = "FILESYSTEM")
})
public abstract class ResourceRegistration {

    @JsonDeserialize(using = EnumDataSource.Deserializer.class)
    protected EnumDataSource source;

    private ResourceMetadataRegistration metadata;

    private TripleGeoConfiguration configuration;

    public ResourceRegistration() {

    }

    public EnumDataSource getSource() {
        return source;
    }

    public ResourceMetadataRegistration getMetadata() {
        return metadata;
    }

    public void setMetadata(ResourceMetadataRegistration metadata) {
        this.metadata = metadata;
    }

    public TripleGeoConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(TripleGeoConfiguration configuration) {
        this.configuration = configuration;
    }

}
