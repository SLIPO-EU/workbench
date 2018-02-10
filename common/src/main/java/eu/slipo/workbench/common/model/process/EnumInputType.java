package eu.slipo.workbench.common.model.process;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Supported process input types
 */
public enum EnumInputType {
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

    private EnumInputType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static EnumInputType fromString(String value) {
        for (EnumInputType item : EnumInputType.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return EnumInputType.UNDEFINED;
    }

    public static class Deserializer extends JsonDeserializer<EnumInputType> {

        @Override
        public EnumInputType deserialize(JsonParser parser, DeserializationContext context)
            throws IOException, JsonProcessingException 
        {
            return EnumInputType.fromString(parser.getValueAsString());
        }
    }
}
