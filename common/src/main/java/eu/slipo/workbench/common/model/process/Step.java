package eu.slipo.workbench.common.model.process;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.springframework.cglib.beans.ImmutableBean;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.github.slugify.Slugify;
import com.google.common.collect.Lists;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumOperation;
import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.resource.DataSource;
import eu.slipo.workbench.common.model.tool.AnyTool;
import eu.slipo.workbench.common.model.tool.DeerConfiguration;
import eu.slipo.workbench.common.model.tool.FagiConfiguration;
import eu.slipo.workbench.common.model.tool.ImportDataConfiguration;
import eu.slipo.workbench.common.model.tool.LimesConfiguration;
import eu.slipo.workbench.common.model.tool.RegisterToCatalogConfiguration;
import eu.slipo.workbench.common.model.tool.ToolConfiguration;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;
import eu.slipo.workbench.common.model.tool.output.OutputPart;

@JsonDeserialize(converter = Step.DeserializeSanitizer.class)
public class Step implements Serializable
{
    private static final long serialVersionUID = 1L;

    protected static final Slugify slugify = new Slugify();
    
    protected static String slugifyName(String name)
    {
        return slugify.slugify(name);
    }
    
    /**
     * A deserialization sanitizer for a {@link Step}
     */
    protected static class DeserializeSanitizer extends StdConverter<Step,Step>
    {
        @Override
        public Step convert(Step step)
        {
            // Sanitize step in-place
            
            // If nodeName is absent, compute it from name
            if (StringUtils.isEmpty(step.nodeName) && !StringUtils.isEmpty(step.name))
                step.nodeName = Step.slugifyName(step.name);
            
            return step;
        }   
    }
    
    /**
     * An input descriptor for a {@link Step}. 
     * 
     * <p>This is not identical to a {@link ProcessInput} because a step may be interested only in a
     * part of an available process-scoped resource (i.e. a part of the output of another step). 
     */
    public static class Input implements Serializable
    {
        private static final long serialVersionUID = 1L;
       
        protected int inputKey;
        
        @Nullable
        protected String partKey;
        
        private Input(int inputKey, String partKey)
        {
            this.inputKey = inputKey;
            this.partKey = partKey;
        }
        
        @JsonProperty("inputKey")
        public int inputKey()
        {
            return inputKey;
        }
        
        @JsonProperty("partKey")
        public String partKey()
        {
            return partKey;
        }
        
        protected static Input of(int inputKey)
        {
            return new Input(inputKey, null);
        }
        
        @JsonCreator
        protected static Input of(
            @JsonProperty("inputKey") int inputKey, @JsonProperty("partKey") String partKey)
        {
            return new Input(inputKey, partKey);
        }

        @Override
        public String toString()
        {
            return String.format(
                "input:%d%s", inputKey, partKey != null? ("/" + partKey) : (""));
        }

        @Override
        public int hashCode()
        {
            final int P = 31;
            int result = 1;
            result = P * result + inputKey;
            result = P * result + (partKey == null? 0 : partKey.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (!(obj instanceof Input))
                return false;
            Input other = (Input) obj;
            if (inputKey != other.inputKey)
                return false;
            return partKey == null? (other.partKey == null): (partKey.equals(other.partKey));
        }
    }
    
    @JsonProperty("key")
    protected int key;

    @JsonProperty("group")
    protected int group;

    @JsonProperty("name")
    protected String name;
    
    @JsonProperty("nodeName")
    protected String nodeName;

    @JsonProperty("operation")
    @JsonDeserialize(using = EnumOperation.Deserializer.class)
    protected EnumOperation operation;

    @JsonProperty("tool")
    @JsonDeserialize(using = EnumTool.Deserializer.class)
    protected EnumTool tool;

    @JsonProperty("input")
    protected List<Input> input = new ArrayList<>();

    @JsonProperty("sources")
    protected List<DataSource> sources = new ArrayList<>();

    @JsonProperty("outputKey")
    protected Integer outputKey;
    
    @JsonProperty("outputFormat")
    protected EnumDataFormat outputFormat;

    @JsonProperty("configuration")
    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "tool")
    @JsonSubTypes({
        @Type(name = "TRIPLEGEO", value = TriplegeoConfiguration.class),
        @Type(name = "LIMES", value = LimesConfiguration.class),
        @Type(name = "FAGI", value = FagiConfiguration.class),
        @Type(name = "DEER", value = DeerConfiguration.class),
        @Type(name = "IMPORTER", value = ImportDataConfiguration.class),
        @Type(name = "REGISTER", value = RegisterToCatalogConfiguration.class)
    })
    protected ToolConfiguration<? extends AnyTool> configuration;

    protected Step() {}

    protected Step(Step other)
    {
        this.key = other.key;
        this.name = other.name;
        this.nodeName = other.nodeName;
        this.group = other.group;
        this.operation = other.operation;
        this.tool = other.tool;
        this.input = other.input;
        this.sources = other.sources;
        this.configuration = other.configuration;
        this.outputFormat = other.outputFormat;
        this.outputKey = other.outputKey;
    }
    
    /**
     * The unique key for this step
     */
    @JsonProperty("key")
    public int key()
    {
        return key;
    }
    
    /**
     * The human-friendly name of this step
     */
    @JsonProperty("name")
    public String name()
    {
        return name;
    }
    
    /**
     * The workflow-friendly name of this step
     */
    @JsonProperty("nodeName")
    public String nodeName()
    {
        return nodeName;
    }

    /**
     * The step group index
     * @return the group index
     */
    @JsonProperty("group")
    public int group()
    {
        return group;
    }

    /**
     * The step operation type
     * @return the operation type
     */
    @JsonProperty("operation")
    public EnumOperation operation()
    {
        return operation;
    }

    /**
     * The tool that implements the operation
     * @return the tool type
     */
    @JsonProperty("tool")
    public EnumTool tool()
    {
        return tool;
    }

    /**
     * The tool-specific configuration
     * @return an instance of {@link ToolConfiguration}
     */
    @JsonProperty("configuration")
    @SuppressWarnings("unchecked")
    public ToolConfiguration<? extends AnyTool> configuration()
    {
        return (ToolConfiguration<? extends AnyTool>) ImmutableBean.create(configuration);
    }
    
    /**
     * The unique resource key of an {@link ProcessOutput} that is the output of
     * this step
     *
     * @return the output resource index
     */
    @JsonProperty("outputKey")
    public Integer outputKey()
    {
        return outputKey;
    }

    /**
     * The descriptor of input resources that should be provided to this step.
     */
    @JsonProperty("input")
    public List<Input> input()
    {
        return Collections.unmodifiableList(input);
    }
    
    /**
     * The keys of input resources that should be provided to this step.
     */
    @JsonIgnore
    public List<Integer> inputKeys()
    {
        return Lists.transform(input, p -> p.inputKey);
    }

    /**
     * A list of external data sources (i.e resources that are neither catalog resources nor
     * intermediate process results) for this step.
     */
    @JsonProperty("sources")
    public List<DataSource> sources()
    {
        return Collections.unmodifiableList(sources);
    }
    
    @JsonProperty("outputFormat")
    public EnumDataFormat outputFormat()
    {
        return outputFormat;
    }
    
    /**
     * A list of available output parts ({@link OutputPart}) provided by this step.
     */
    @JsonIgnore
    public List<OutputPart<? extends AnyTool>> outputParts()
    {
        return tool.getOutputParts();
    }
    
    /**
     * Get an output part ({@link OutputPart}).
     * 
     * @param partKey The part key to search by; may be <tt>null</tt>, and in such a case it 
     *   will be behave exactly as {@link Step#defaultOutputPart()}
     */
    @JsonIgnore
    public OutputPart<? extends AnyTool> outputPart(String partKey)
    {
        if (StringUtils.isEmpty(partKey))
            return tool.getDefaultOutputPart();
        
        Optional<OutputPart<? extends AnyTool>> part = tool.getOutputParts().stream()
            .filter(p -> p.key().equals(partKey))
            .findFirst();
        
        return part.orElse(null);
    }
    
    /**
     * Get the default output part ({@link OutputPart}) of this this step (may be <tt>null</tt>
     * if this step doesn't produce any output).
     */
    @JsonIgnore
    public OutputPart<? extends AnyTool> defaultOutputPart()
    {
        return tool.getDefaultOutputPart();
    }
    
    @Override
    public String toString()
    {
        return String.format(
            "Step [key=%s, name=%s, operation=%s, tool=%s, outputFormat=%s]", 
            key, name, operation, tool, outputFormat);
    }
}
