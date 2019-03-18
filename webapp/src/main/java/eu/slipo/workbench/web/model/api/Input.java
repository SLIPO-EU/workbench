package eu.slipo.workbench.web.model.api;


import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * API input
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
    @Type(name = "FILESYSTEM", value = FileInput.class),
    @Type(name = "CATALOG", value = CatalogInput.class),
})
public abstract class Input implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum EnumType {
        FILESYSTEM,
        CATALOG,
        ;
    }

    protected EnumType type;

    protected Input() {

    }

    protected Input(EnumType type) {
        this.type = type;
    }

    public EnumType getType() {
        return type;
    }

    public void setType(EnumType type) {
        this.type = type;
    }

}
