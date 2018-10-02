package eu.slipo.workbench.common.model.process;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Supported tasks
 */
public enum EnumProcessTaskType {
    /**
     * Invalid process task
     */
    UNDEFINED(0),

    /**
     * A resource registration process created using the workbench resource wizard
     */
    REGISTRATION(1),

    /**
     * A generic data integration process created using the workbench process designer
     */
    DATA_INTEGRATION(2),

    /**
     * A resource export process created using the workbench export wizard
     */
    EXPORT(3),
    ;

    private final int value;

    private EnumProcessTaskType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static EnumProcessTaskType fromString(String value) {
        for (EnumProcessTaskType item : EnumProcessTaskType.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return EnumProcessTaskType.UNDEFINED;
    }

    public static class Deserializer extends JsonDeserializer<EnumProcessTaskType> {

        @Override
        public EnumProcessTaskType deserialize(JsonParser parser, DeserializationContext context)
            throws IOException, JsonProcessingException
        {
            return EnumProcessTaskType.fromString(parser.getValueAsString());
        }
    }
}
