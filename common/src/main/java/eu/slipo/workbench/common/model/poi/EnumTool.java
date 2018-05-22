package eu.slipo.workbench.common.model.poi;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Enumerate SLIPO toolkit components
 */
public enum EnumTool 
{
    /**
     * Unknown tool
     */
    UNDEFINED(0),
    
    /**
     * Catalog registration component
     */
    REGISTER(1, EnumOperation.REGISTER),
    
    /**
     * Data transformation component
     */
    TRIPLEGEO(2, EnumOperation.TRANSFORM),
    
    /**
     * POI RDF dataset interlinking component
     */
    LIMES(3, EnumOperation.INTERLINK),
    
    /**
     * POI RDF dataset and linked data fusion component
     */
    FAGI(4, EnumOperation.FUSION),
    
    /**
     * POI RDF dataset enrichment component
     */
    DEER(5, EnumOperation.ENRICHMENT),
    
    /**
     * An internal component for importing external data sources into a process
     */
    IMPORTER(6, EnumOperation.IMPORT)
    ;

    /**
     * An integer code
     */
    private final int value;

    /**
     * The set of operations supported by this tool
     */
    private final Set<EnumOperation> operations;
    
    private EnumTool(int value) 
    {
        this.value = value;
        this.operations = Collections.emptySet();
    }

    private EnumTool(int value, EnumOperation op1) 
    {
        this.value = value;
        this.operations = Collections.singleton(op1);
    }
    
    private EnumTool(int value, EnumOperation op1, EnumOperation op2) 
    {
        this.value = value;
        this.operations = Collections.unmodifiableSet(EnumSet.of(op1, op2));
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
    
    public boolean supportsOperation(EnumOperation op)
    {
        return op != null && this.operations.contains(op);
    }
    
    public Set<EnumOperation> getSupportedOperations()
    {
        return operations;
    }
        
    public static EnumTool fromString(String value) {
        for (EnumTool item : EnumTool.values()) {
            if (item.name().equalsIgnoreCase(value))
                return item;
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
