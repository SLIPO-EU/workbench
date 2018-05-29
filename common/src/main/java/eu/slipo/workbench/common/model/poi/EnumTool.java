package eu.slipo.workbench.common.model.poi;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.google.common.collect.Lists;

import eu.slipo.workbench.common.model.tool.output.OutputPart;
import eu.slipo.workbench.common.model.tool.output.EnumImportDataOutputPart;
import eu.slipo.workbench.common.model.tool.AnyTool;
import eu.slipo.workbench.common.model.tool.RegisterToCatalog;
import eu.slipo.workbench.common.model.tool.Triplegeo;
import eu.slipo.workbench.common.model.tool.Limes;
import eu.slipo.workbench.common.model.tool.Fagi;
import eu.slipo.workbench.common.model.tool.ImportData;
import eu.slipo.workbench.common.model.tool.Deer;
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
     * Catalog registration component
     */
    REGISTER(
        RegisterToCatalog.class, 
        EnumOperation.REGISTER),
    
    /**
     * Data transformation component
     */
    TRIPLEGEO(
        Triplegeo.class, 
        EnumOperation.TRANSFORM, 
        EnumTriplegeoOutputPart.class,
        EnumTriplegeoOutputPart.TRANSFORMED),
    
    /**
     * POI RDF dataset interlinking component
     */
    LIMES(
        Limes.class, 
        EnumOperation.INTERLINK, 
        EnumLimesOutputPart.class, 
        EnumLimesOutputPart.ACCEPTED),
    
    /**
     * POI RDF dataset and linked data fusion component
     */
    FAGI(
        Fagi.class, 
        EnumOperation.FUSION, 
        EnumFagiOutputPart.class, 
        EnumFagiOutputPart.FUSED),
    
    /**
     * POI RDF dataset enrichment component
     */
    DEER(
        Deer.class, 
        EnumOperation.ENRICHMENT,
        EnumDeerOutputPart.class, 
        EnumDeerOutputPart.ENRICHED),
    
    /**
     * An internal component for importing external data sources into a process
     */
    IMPORTER(
        ImportData.class,
        EnumOperation.IMPORT_DATA, 
        EnumImportDataOutputPart.class,
        EnumImportDataOutputPart.DOWNLOAD)
    ;

    /**
     * A marker type for this tool
     */
    private final Class<? extends AnyTool> type;
    
    /**
     * The set of operations supported by this tool
     */
    private final Set<EnumOperation> operations;
    
    /**
     * The enumeration type that describes parts of the output of a tool invocation
     */
    private final Class<? extends OutputPart<? extends AnyTool>> outputPartEnumeration;

    /**
     * The list of output parts
     */
    private final List<OutputPart<? extends AnyTool>> outputParts;
    
    /**
     * The default output part
     */
    private final OutputPart<? extends AnyTool> defaultOutputPart;
   
    private <T extends AnyTool> EnumTool(Class<T> toolType, EnumOperation op1) 
    {
        Assert.notNull(toolType, "A marker type is required");
        Assert.notNull(op1, "An operation constant is required");
        this.type = toolType;
        this.operations = Collections.singleton(op1);
        this.outputPartEnumeration = null;
        this.outputParts = null;
        this.defaultOutputPart = null;
    }

    private <T extends AnyTool, P extends Enum<P> & OutputPart<T>> EnumTool(
        Class<T> toolType, EnumOperation op1, Class<P> outputPartEnumeration, P defaultOutputPart) 
    {
        Assert.notNull(toolType, "A marker type is required");
        Assert.notNull(op1, "An operation constant is required");
        Assert.notNull(outputPartEnumeration, "Expected an enumeration of output parts");
        Assert.notNull(defaultOutputPart, "Expected a default part (inside given enumeration)");
        Assert.isTrue(EnumOutputType.OUTPUT.equals(defaultOutputPart.outputType()), 
            "A default output part must be of OUTPUT type");
        this.type = toolType;
        this.operations = Collections.singleton(op1);
        this.outputPartEnumeration = outputPartEnumeration;
        this.outputParts = Collections.unmodifiableList(
            Arrays.asList(outputPartEnumeration.getEnumConstants()));
        this.defaultOutputPart = defaultOutputPart;
    }
    
    public String getKey() 
    {
        return (this.getClass().getSimpleName() + '.' + name());
    }

    public Class<? extends AnyTool> getType()
    {
        return type;
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
    
    public Class<? extends OutputPart<? extends AnyTool>> getOutputPartEnumeration()
    {
        return outputPartEnumeration;
    }

    public List<OutputPart<? extends AnyTool>> getOutputParts()
    {
        return outputParts;
    }
    
    public OutputPart<? extends AnyTool> getDefaultOutputPart()
    {
        return defaultOutputPart;
    }
    
    public static EnumTool fromName(String name) 
    {
        Assert.isTrue(!StringUtils.isEmpty(name), "A non-empty name is required");
        for (EnumTool t : EnumTool.values())
            if (t.name().equalsIgnoreCase(name))
                return t;
        return null;
    }
    
    public static <T extends AnyTool> EnumTool fromType(Class<T> type)
    {
        Assert.notNull(type, "A type is required");
        return typeMap.get(type);
    }

    private static Map<Class<? extends AnyTool>, EnumTool> typeMap = new IdentityHashMap<>();

    static {
        for (EnumTool t: EnumTool.values())
            typeMap.put(t.type, t);
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
