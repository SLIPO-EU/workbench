package eu.slipo.workbench.web.model;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Supported resource types
 */
public enum EnumResourceType {
    /**
     * Invalid type
     */
    UNDEFINED(0),
    /**
     * POI RDF dataset
     */
    POI_DATA(1),
    /**
     * POI linked data
     */
    POI_LINKED_DATA(2),
    ;

    private final int value;

    private EnumResourceType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public String getKey() {
        return (this.getClass().getSimpleName() + '.' + name());
    }

    public static EnumResourceType fromString(String value) {
        for (EnumResourceType item : EnumResourceType.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return EnumResourceType.UNDEFINED;
    }

    public static class Deserializer extends JsonDeserializer<EnumResourceType> {

        @Override
        public EnumResourceType deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
            return EnumResourceType.fromString(parser.getValueAsString());
        }
    }
}
