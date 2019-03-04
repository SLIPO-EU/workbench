package eu.slipo.workbench.common.model.process;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.springframework.cglib.beans.ImmutableBean;
import org.springframework.util.Assert;
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
import eu.slipo.workbench.common.model.tool.ReverseTriplegeoConfiguration;
import eu.slipo.workbench.common.model.tool.ToolConfiguration;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;
import eu.slipo.workbench.common.model.tool.output.EnumOutputType;
import eu.slipo.workbench.common.model.tool.output.OutputPart;

@JsonDeserialize(converter = Step.DeserializeConverter.class)
public class Step implements Serializable
{
    private static final long serialVersionUID = 1L;

    protected static final Slugify slugify = new Slugify();

    protected static String slugifyName(String name)
    {
        return slugify.slugify(name);
    }

    /**
     * A deserialization converter for a {@link Step}
     */
    protected static class DeserializeConverter extends StdConverter<Step,Step>
    {
        @Override
        public Step convert(Step step)
        {
            // Initialize step in-place
            step.initialize();
            return step;
        }
    }

    /**
     * An input descriptor for a {@link Step}.
     *
     * <p>This is not identical to a {@link ProcessInput} because a step may be interested only in a
     * part of an available process-wide resource (i.e. a part of the output of another step).
     */
    public final static class Input implements Serializable
    {
        private static final long serialVersionUID = 1L;

        protected final String inputKey;

        @Nullable
        protected final String partKey;

        @JsonIgnore
        @Nullable
        protected final OutputPart<? extends AnyTool> part;

        private Input(String inputKey, String partKey)
        {
            this.inputKey = inputKey;
            this.partKey = partKey;
            this.part = null;
        }

        private Input(String inputKey, OutputPart<? extends AnyTool> part)
        {
            this.inputKey = inputKey;
            this.partKey = part.key();
            this.part = part;
        }

        @JsonProperty("inputKey")
        public String inputKey()
        {
            return inputKey;
        }

        @JsonProperty("partKey")
        public String partKey()
        {
            return partKey;
        }

        protected OutputPart<? extends AnyTool> part()
        {
            return part;
        }

        protected static Input of(String inputKey)
        {
            Assert.isTrue(!StringUtils.isEmpty(inputKey), "An non-empty input key is expected");
            return new Input(inputKey, (String) null);
        }

        @JsonCreator
        protected static Input of(
            @JsonProperty("inputKey") String inputKey, @JsonProperty("partKey") String partKey)
        {
            Assert.isTrue(!StringUtils.isEmpty(inputKey), "An non-empty input key is expected");
            return new Input(inputKey, partKey);
        }

        protected static Input of(String inputKey, OutputPart<? extends AnyTool> part)
        {
            Assert.isTrue(!StringUtils.isEmpty(inputKey), "An non-empty input key is expected");
            Assert.notNull(part, "An output part is required");

            if (part.outputType() != EnumOutputType.OUTPUT) {
                throw new IllegalArgumentException(
                    String.format("A step may receive as input only parts of output of type [%s], not [%s]",
                        EnumOutputType.OUTPUT, part.outputType()));
            }

            return new Input(inputKey, part);
        }

        @Override
        public String toString()
        {
            return String.format("input:%s%s",
                inputKey, partKey != null? ("/" + partKey) : (""));
        }

        @Override
        public int hashCode()
        {
            final int P = 31;
            int result = 1;
            result = P * result + inputKey.hashCode();
            result = P * result + (partKey == null? 0 : partKey.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Input)) {
                return false;
            }
            Input other = (Input) obj;
            if (!inputKey.equals(other.inputKey)) {
                return false;
            }
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
    protected String outputKey;

    @JsonProperty("outputFormat")
    protected EnumDataFormat outputFormat;

    /**
     * The user-provided configuration to drive a tool's invocation.
     */
    @JsonProperty("configuration")
    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "tool")
    @JsonSubTypes({
        @Type(name = "TRIPLEGEO", value = TriplegeoConfiguration.class),
        @Type(name = "LIMES", value = LimesConfiguration.class),
        @Type(name = "FAGI", value = FagiConfiguration.class),
        @Type(name = "DEER", value = DeerConfiguration.class),
        @Type(name = "IMPORTER", value = ImportDataConfiguration.class),
        @Type(name = "REGISTER", value = RegisterToCatalogConfiguration.class),
        @Type(name = "REVERSE_TRIPLEGEO", value = ReverseTriplegeoConfiguration.class)
    })
    protected ToolConfiguration<? extends AnyTool> configuration;

    /**
     * An unmodifiable view of user-provided configuration.
     */
    @JsonIgnore
    private ToolConfiguration<? extends AnyTool> _configuration = null;

    protected Step() {}

    protected Step(int key)
    {
        this.key = key;
    }

    protected static Step of(int key, Step other)
    {
        Step step = new Step(key);

        step.name = other.name;
        step.nodeName = other.nodeName;
        step.group = other.group;
        step.operation = other.operation;
        step.tool = other.tool;
        step.input = other.input;
        step.sources = other.sources;
        step.configuration = other.configuration;
        step.outputFormat = other.outputFormat;
        step.outputKey = other.outputKey;

        step.initialize();
        return step;
    }

    /**
     * A hook to perform post-construct initialization.
     * <p>This post-processing is idempotent, but not thread-safe!
     */
    protected void initialize()
    {
        // If nodeName is absent, compute it from name

        if (StringUtils.isEmpty(this.nodeName) && !StringUtils.isEmpty(this.name)) {
            this.nodeName = Step.slugifyName(this.name);
        }

        // Create an unmodifiable view of configuration

        if (this._configuration == null) {
            @SuppressWarnings("unchecked")
            ToolConfiguration<? extends AnyTool> configuration =
                (ToolConfiguration<? extends AnyTool>) ImmutableBean.create(this.configuration);
            this._configuration = configuration;
        }
    }

    /**
     * The unique key for this step
     */
    public int key()
    {
        return key;
    }

    /**
     * The human-friendly name of this step
     */
    public String name()
    {
        return name;
    }

    /**
     * The workflow-friendly name of this step
     */
    public String nodeName()
    {
        return nodeName;
    }

    /**
     * The step group index
     * @return the group index
     */
    public int group()
    {
        return group;
    }

    /**
     * The step operation type
     * @return the operation type
     */
    public EnumOperation operation()
    {
        return operation;
    }

    /**
     * The tool that implements the operation
     * @return the tool type
     */
    public EnumTool tool()
    {
        return tool;
    }

    /**
     * The tool-specific user-provided configuration
     * @return an unmodifiable instance of {@link ToolConfiguration}
     */
    public ToolConfiguration<? extends AnyTool> configuration()
    {
        return _configuration;
    }

    /**
     * The unique resource key of an {@link ProcessOutput} that is the output of
     * this step
     *
     * @return the output resource index
     */
    public String outputKey()
    {
        return outputKey;
    }

    /**
     * The descriptor of input resources that should be provided to this step.
     */
    public List<Input> input()
    {
        return Collections.unmodifiableList(input);
    }

    /**
     * The keys of input resources that should be provided to this step.
     */
    public List<String> inputKeys()
    {
        return Lists.transform(input, p -> p.inputKey);
    }

    /**
     * A list of external data sources (i.e resources that are neither catalog resources nor
     * intermediate process results) for this step.
     */
    public List<DataSource> sources()
    {
        return Collections.unmodifiableList(sources);
    }

    public EnumDataFormat outputFormat()
    {
        return outputFormat;
    }

    /**
     * A list of available output parts ({@link OutputPart}) provided by this step.
     */
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
    public OutputPart<? extends AnyTool> outputPart(String partKey)
    {
        return StringUtils.isEmpty(partKey)?
            tool.getDefaultOutputPart() : tool.getOutputPart(partKey).orElse(null);
    }

    /**
     * Get the default output part ({@link OutputPart}) of this this step (may be <tt>null</tt>
     * if this step doesn't produce any output).
     */
    public OutputPart<? extends AnyTool> defaultOutputPart()
    {
        return tool.getDefaultOutputPart();
    }

    @Override
    public String toString()
    {
        return String.format(
            "Step [key=%s, nodeName=%s, operation=%s, tool=%s, outputFormat=%s]",
            key, nodeName, operation, tool, outputFormat);
    }

    // TODO: Review implementation

    /**
     * Provides access to a configuration object that is not immutable
     *
     * @return the step configuration
     */
    @JsonIgnore()
    public ToolConfiguration<? extends AnyTool> getConfigurationUnsafe()
    {
        return this.configuration;
    }

}
