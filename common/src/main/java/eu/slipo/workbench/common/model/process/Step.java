package eu.slipo.workbench.common.model.process;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.slipo.workbench.common.model.poi.EnumOperation;
import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.resource.DataSource;
import eu.slipo.workbench.common.model.tool.MetadataRegistrationConfiguration;
import eu.slipo.workbench.common.model.tool.ToolConfiguration;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;

public class Step implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty("key")
    private int key;

    @JsonProperty("group")
    private int group;

    @JsonProperty("name")
    private String name;

    @JsonProperty("operation")
    @JsonDeserialize(using = EnumOperation.Deserializer.class)
    private EnumOperation operation;

    @JsonProperty("tool")
    @JsonDeserialize(using = EnumTool.Deserializer.class)
    private EnumTool tool;

    @JsonProperty("input")
    private List<Integer> input = new ArrayList<Integer>();

    @JsonProperty("sources")
    private List<DataSource> sources = new ArrayList<>();

    @JsonProperty("outputKey")
    private Integer outputKey;

    @JsonProperty("configuration")
    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
    @JsonSubTypes({
        @Type(name = "TRIPLEGEO", value = TriplegeoConfiguration.class),
        @Type(name = "REGISTER-METADATA", value = MetadataRegistrationConfiguration.class)
    })
    private ToolConfiguration configuration;

    protected Step() {}

    public static class Builder
    {
        private final int key;

        private final int group;

        private final String name;

        private EnumOperation operation;

        private EnumTool tool;

        private List<Integer> input = new ArrayList<>();

        private List<DataSource> sources = new ArrayList<>();

        private Integer outputKey;

        private ToolConfiguration configuration;

        /**
         * Create a builder for a step ({@link Step}).
         *
         * @param key A unique (across its process) name for this step
         * @param group A group index for this step. This group is only a logical grouping
         *    of steps inside a process.
         * @param name A name (preferably unique) for this step.
         */
        public Builder(int key, int group, String name)
        {
            Assert.isTrue(!StringUtils.isEmpty(name),
                "Expected a non-empty name for this step");
            this.key = key;
            this.group = group;
            this.name = name;
        }

        /**
         * Set the operation type.
         * @param operation
         */
        public Builder operation(EnumOperation operation)
        {
            Assert.notNull(operation, "Expected an non-null operation");
            this.operation = operation;
            return this;
        }

        /**
         * Set the tool that implements the step operation
         * @param tool
         */
        public Builder tool(EnumTool tool)
        {
            Assert.notNull(tool, "Expected a tool constant");
            this.tool = tool;
            return this;
        }

        /**
         * Provide the tool-specific configuration
         * @param configuration A tool-specific configuration bean
         */
        public Builder configuration(ToolConfiguration configuration)
        {
            Assert.notNull(configuration, "Expected a non-null configuration");
            this.configuration = configuration;
            return this;
        }

        /**
         * Assign a unique key to the resource generated as output of this step inside
         * a process.
         * @param outputKey
         */
        public Builder outputKey(int outputKey)
        {
            this.outputKey = outputKey;
            return this;
        }

        /**
         * Set the keys of process-wide resources that should be input to this step
         * @param inputKeys A list of resource keys
         */
        public Builder input(List<Integer> inputKeys)
        {
            this.input.addAll(inputKeys);
            return this;
        }

        /**
         * @see {@link Step.Builder#input(List)}
         * @param inputKey
         */
        public Builder input(int inputKey)
        {
            this.input.add(inputKey);
            return this;
        }

        /**
         * Set a list of external data sources that should be input to this step.
         *
         * <p>A {@link DataSource} is an input that is external to the application, i.e it is
         * neither a catalog resource nor a intermediate result of the (enclosing) process.
         *
         * @param s A list of data sources
         */
        public Builder source(List<DataSource> s)
        {
            this.sources.addAll(s);
            return this;
        }

        /**
         * @see {@link Step.Builder#source(List)}
         * @param s
         */
        public Builder source(DataSource s)
        {
            this.sources.add(s);
            return this;
        }

        public Step build()
        {
            Assert.state(this.operation != null, "The operation must be specified");
            Assert.state(this.tool != null, "The tool must be specified");
            Assert.state(this.configuration != null, "The tool configuration must be provided");
            Assert.state(this.outputKey != null || operation == EnumOperation.REGISTER,
                "An output key is required for a non-registration step");
            Assert.state(!this.input.isEmpty() || !this.sources.isEmpty(),
                "The list of data sources and list of input keys cannot be both empty!");

            Step step = new Step();

            step.key = this.key;
            step.group = this.group;
            step.name = this.name;
            step.operation = this.operation;
            step.tool = this.tool;
            step.sources = new ArrayList<>(this.sources);
            step.input = new ArrayList<>(this.input);
            step.outputKey = this.outputKey;

            try {
                // Make a defensive copy of the configuration bean
                step.configuration = (ToolConfiguration) BeanUtils.cloneBean(configuration);
            } catch (ReflectiveOperationException ex) {
                throw new IllegalStateException("Cannot clone configuration bean", ex);
            }

            return step;
        }
    }

    public static Builder builder(int key, int group, String name)
    {
        return new Builder(key, group, name);
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
     * The name of this step
     */
    @JsonProperty("name")
    public String name()
    {
        return name;
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
    public ToolConfiguration configuration()
    {
        return configuration;
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
     * The keys of input resources that should be provided to this step.
     */
    @JsonProperty("input")
    public List<Integer> input()
    {
        return Collections.unmodifiableList(input);
    }

    /**
     * A list of external data sources (i.e neither catalog resources nor intermediate
     * process results) for this step.
     */
    @JsonProperty("sources")
    public List<DataSource> sources()
    {
        return sources;
    }
}
