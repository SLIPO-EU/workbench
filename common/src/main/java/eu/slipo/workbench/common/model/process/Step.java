package eu.slipo.workbench.common.model.process;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumOperation;
import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.resource.DataSource;
import eu.slipo.workbench.common.model.tool.DeerConfiguration;
import eu.slipo.workbench.common.model.tool.FagiConfiguration;
import eu.slipo.workbench.common.model.tool.LimesConfiguration;
import eu.slipo.workbench.common.model.tool.MetadataRegistrationConfiguration;
import eu.slipo.workbench.common.model.tool.ToolConfiguration;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;

public class Step implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty("key")
    protected int key;

    @JsonProperty("group")
    protected int group;

    @JsonProperty("name")
    protected String name;

    @JsonProperty("operation")
    @JsonDeserialize(using = EnumOperation.Deserializer.class)
    protected EnumOperation operation;

    @JsonProperty("tool")
    @JsonDeserialize(using = EnumTool.Deserializer.class)
    protected EnumTool tool;

    @JsonProperty("inputKeys")
    protected List<Integer> inputKeys = new ArrayList<Integer>();

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
        @Type(name = "REGISTER_METADATA", value = MetadataRegistrationConfiguration.class)
    })
    protected ToolConfiguration configuration;

    protected Step() {}

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
    @JsonProperty("inputKeys")
    public List<Integer> inputKeys()
    {
        return Collections.unmodifiableList(inputKeys);
    }

    /**
     * A list of external data sources (i.e neither catalog resources nor intermediate
     * process results) for this step.
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
}
