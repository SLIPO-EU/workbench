package eu.slipo.workbench.web.model.process;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Supported process input types
 */
public enum EnumProcessResource {
    /**
     * Invalid resource type
     */
    UNDEFINED(0),
    /**
     * An existing resource already registered in the catalog
     */
    CATALOG(1),
    /**
     * The output of an intermediate step
     */
    OUTPUT(2),
    ;

    private final int value;

    private EnumProcessResource(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static EnumProcessResource fromString(String value) {
        for (EnumProcessResource item : EnumProcessResource.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return EnumProcessResource.UNDEFINED;
    }

    public static class Deserializer extends JsonDeserializer<EnumProcessResource> {

        @Override
        public EnumProcessResource deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
            return EnumProcessResource.fromString(parser.getValueAsString());
        }
    }
}
