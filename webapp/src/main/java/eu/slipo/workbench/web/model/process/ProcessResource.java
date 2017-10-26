package eu.slipo.workbench.web.model.process;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.slipo.workbench.web.model.EnumDataSource;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
    @Type(value = FileProcessResource.class, name = "FILE"),
    @Type(value = CatalogProcessResource.class, name = "CATALOG"),
    @Type(value = TransientProcessResource.class, name = "TRANSIENT"),
    @Type(value = OutputProcessResource.class, name = "OUTPUT"),
})
public abstract class ProcessResource {

    @JsonDeserialize(using = EnumDataSource.Deserializer.class)
    protected EnumProcessResource type;

    protected int index;

    public ProcessResource() {

    }

    public ProcessResource(int index, EnumProcessResource type) {
        this.index = index;
        this.type = type;
    }

    public EnumProcessResource getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }

}
