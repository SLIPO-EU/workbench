package eu.slipo.workbench.web.model.process;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * A process input resource
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
    @Type(value = CatalogProcessResource.class, name = "CATALOG"),
    @Type(value = OutputProcessResource.class, name = "OUTPUT"),
})
public abstract class ProcessResource {

    @JsonDeserialize(using = EnumProcessResource.Deserializer.class)
    protected EnumProcessResource type;

    protected int index;

    protected ProcessResource() {

    }

    protected ProcessResource(EnumProcessResource type) {
        this.type = type;
    }

    protected ProcessResource(int index, EnumProcessResource type) {
        this.index = index;
        this.type = type;
    }

    /**
     * Resource type
     *
     * @return the type of the resource
     */
    public EnumProcessResource getType() {
        return type;
    }

    /**
     * The unique index of the resource
     *
     * @return the resource index
     */
    public int getIndex() {
        return index;
    }

}
