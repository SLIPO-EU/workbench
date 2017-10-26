package eu.slipo.workbench.web.model;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Supported POI data integration operations
 */
public enum EnumOperation {
    /**
     * Invalid operation
     */
    UNDEFINED(0),
    /**
     * Register resource to catalog
     */
    REGISTER(1),
    /**
     * Data transformation
     */
    TRANSFORM(2),
    /**
     * POI RDF dataset interlinking
     */
    INTERLINK(3),
    /**
     * POI RDF dataset and linked data fusion
     */
    FUSION(4),
    /**
     * POI RDF dataset enrichment
     */
    ENRICHEMENT(5);

    private final int value;

    private EnumOperation(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public String getKey() {
        return (this.getClass().getSimpleName() + '.' + name());
    }

    public static EnumOperation fromString(String value) {
        for (EnumOperation item : EnumOperation.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return EnumOperation.UNDEFINED;
    }

    public static class Deserializer extends JsonDeserializer<EnumOperation> {

        @Override
        public EnumOperation deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
            return EnumOperation.fromString(parser.getValueAsString());
        }
    }
}
