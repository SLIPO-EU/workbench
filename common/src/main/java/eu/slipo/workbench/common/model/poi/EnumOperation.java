package eu.slipo.workbench.common.model.poi;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Supported POI data integration operations
 */
public enum EnumOperation 
{    
    /**
     * Register resource to catalog
     */
    REGISTER,
    
    /**
     * Data transformation
     */
    TRANSFORM,
    
    /**
     * POI RDF dataset interlinking
     */
    INTERLINK,
    
    /**
     * POI RDF dataset and linked data fusion
     */
    FUSION,
    
    /**
     * POI RDF dataset enrichment
     */
    ENRICHMENT,
    
    /**
     * Import external data sources into a process
     */
    IMPORT_DATA;

    private EnumOperation() {
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
        return null;
    }

    public static class Deserializer extends JsonDeserializer<EnumOperation> {

        @Override
        public EnumOperation deserialize(JsonParser parser, DeserializationContext context) 
            throws IOException, JsonProcessingException 
        {
            return EnumOperation.fromString(parser.getValueAsString());
        }
    }
}
