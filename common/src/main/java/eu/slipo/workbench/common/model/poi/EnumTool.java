package eu.slipo.workbench.common.model.poi;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Enumerate SLIPO toolkit components
 */
public enum EnumTool {
    /**
     * Unknown tool
     */
    UNDEFINED(0),
    /**
     * Catalog management component
     */
    REGISTER_METADATA(1),
    /**
     * Data transformation component
     */
    TRIPLEGEO(2),
    /**
     * POI RDF dataset interlinking component
     */
    LIMES(3),
    /**
     * POI RDF dataset and linked data fusion component
     */
    FAGI(4),
    /**
     * POI RDF dataset enrichment component
     */
    DEER(5),
    ;

    private final int value;

    private EnumTool(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public String getKey() {
        return (this.getClass().getSimpleName() + '.' + name());
    }

    public EnumResourceType getResourceType()
    {
        switch (this) {
        case TRIPLEGEO:
        case DEER:
        case FAGI:
            return EnumResourceType.POI_DATA;
        case LIMES:
            return EnumResourceType.POI_LINKED_DATA;
        default:
            return EnumResourceType.UNDEFINED;
        }
    }
    
    public static EnumTool fromString(String value) {
        for (EnumTool item : EnumTool.values()) {
            if (item.name().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return EnumTool.UNDEFINED;
    }

    public static class Deserializer extends JsonDeserializer<EnumTool> {

        @Override
        public EnumTool deserialize(JsonParser parser, DeserializationContext context) 
            throws IOException, JsonProcessingException 
        {
            return EnumTool.fromString(parser.getValueAsString());
        }
    }
}
