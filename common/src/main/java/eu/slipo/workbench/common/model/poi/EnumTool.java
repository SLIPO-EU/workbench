package eu.slipo.workbench.common.model.poi;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import eu.slipo.workbench.common.model.tool.output.OutputPart;
import eu.slipo.workbench.common.model.tool.output.EnumImportOutputPart;
import eu.slipo.workbench.common.model.tool.output.EnumDeerOutputPart;
import eu.slipo.workbench.common.model.tool.output.EnumFagiOutputPart;
import eu.slipo.workbench.common.model.tool.output.EnumTriplegeoOutputPart;
import eu.slipo.workbench.common.model.tool.output.EnumLimesOutputPart;
import eu.slipo.workbench.common.model.tool.output.EnumOutputType;

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
    TRIPLEGEO(2, EnumOperation.TRANSFORM, EnumTriplegeoOutputPart.class, EnumTriplegeoOutputPart.TRANSFORMED),
    
    /**
     * POI RDF dataset interlinking component
     */
    LIMES(3, EnumOperation.INTERLINK, EnumLimesOutputPart.class, EnumLimesOutputPart.ACCEPTED),
    
    /**
     * POI RDF dataset and linked data fusion component
     */
    FAGI(4, EnumOperation.FUSION, EnumFagiOutputPart.class, EnumFagiOutputPart.FUSED),
    
    /**
     * POI RDF dataset enrichment component
     */
    DEER(5, EnumOperation.ENRICHMENT, EnumDeerOutputPart.class, EnumDeerOutputPart.ENRICHED),
    
    /**
     * An internal component for importing external data sources into a process
     */
    IMPORTER(6, EnumOperation.IMPORT, EnumImportOutputPart.class, EnumImportOutputPart.DOWNLOAD)
    ;

    /**
     * An integer code
     */
    private final int value;

    /**
     * The set of operations supported by this tool
     */
    private final Set<EnumOperation> operations;
    
    /**
     * The enumeration type that describes parts of the output of a tool invocation
     */
    private final Class<? extends OutputPart> outputPartEnumeration;

    /**
     * The list of output parts
     */
    private final List<OutputPart> outputParts;
    
    /**
     * The default output part
     */
    private final OutputPart defaultOutputPart;
   
    private EnumTool(int value) 
    {
        this.value = value;
        this.operations = Collections.emptySet();
        this.outputPartEnumeration = null;
        this.outputParts = null;
        this.defaultOutputPart = null;
    }
    
    private EnumTool(int value, EnumOperation op1) 
    {
        Assert.notNull(op1, "An operation constant is required");
        this.value = value;
        this.operations = Collections.singleton(op1);
        this.outputPartEnumeration = null;
        this.outputParts = null;
        this.defaultOutputPart = null;
    }

    private <T extends Enum<T> & OutputPart> EnumTool(
        int value, EnumOperation op1, Class<T> outputPartEnumeration, T defaultOutputPart) 
    {
        Assert.notNull(op1, "An operation constant is required");
        Assert.notNull(outputPartEnumeration, "Expected an enumeration of output parts");
        Assert.notNull(defaultOutputPart, "Expected a default part (inside given enumeration)");
        Assert.isTrue(EnumOutputType.OUTPUT.equals(defaultOutputPart.outputType()), 
            "A default output part must be of OUTPUT type");
        this.value = value;
        this.operations = Collections.singleton(op1);
        this.outputPartEnumeration = outputPartEnumeration;
        this.outputParts = Collections.unmodifiableList(
            Arrays.asList(outputPartEnumeration.getEnumConstants()));
        this.defaultOutputPart = defaultOutputPart;
    }
    
    public int getValue() 
    {
        return value;
    }

    public String getKey() 
    {
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
    
    public Class<? extends OutputPart> getOutputPartEnumeration()
    {
        return outputPartEnumeration;
    }

    public List<OutputPart> getOutputParts()
    {
        return outputParts;
    }
    
    public OutputPart getDefaultOutputPart()
    {
        return defaultOutputPart;
    }
    
    public static EnumTool fromName(String name) 
    {
        for (EnumTool item : EnumTool.values()) {
            if (item.name().equalsIgnoreCase(name))
                return item;
        }
        return EnumTool.UNDEFINED;
    }

    public static class Deserializer extends JsonDeserializer<EnumTool> {

        @Override
        public EnumTool deserialize(JsonParser parser, DeserializationContext context) 
            throws IOException, JsonProcessingException 
        {
            return EnumTool.fromName(parser.getValueAsString());
        }
    }
}
