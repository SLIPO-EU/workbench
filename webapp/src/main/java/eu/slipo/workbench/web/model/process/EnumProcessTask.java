package eu.slipo.workbench.web.model.process;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Supported tasks
 */
public enum EnumProcessTask {
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
    ;

    private final int value;

    private EnumProcessTask(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static EnumProcessTask fromString(String value) {
        for (EnumProcessTask item : EnumProcessTask.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return EnumProcessTask.UNDEFINED;
    }

    public static class Deserializer extends JsonDeserializer<EnumProcessTask> {

        @Override
        public EnumProcessTask deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
            return EnumProcessTask.fromString(parser.getValueAsString());
        }
    }
}
