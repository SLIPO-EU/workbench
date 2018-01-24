package eu.slipo.workbench.web.model.process;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.slipo.workbench.web.model.EnumOperation;
import eu.slipo.workbench.web.model.EnumTool;

public class Step {

    private int key;

    private int group;

    private String name;

    @JsonDeserialize(using = EnumOperation.Deserializer.class)
    private EnumOperation operation;

    @JsonDeserialize(using = EnumTool.Deserializer.class)
    private EnumTool tool;

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "tool")
    @JsonSubTypes({
        @Type(value = TripleGeoConfiguration.class, name = "TRIPLE_GEO"),
        @Type(value = LimesConfiguration.class, name = "LIMES"),
        @Type(value = FagiConfiguration.class, name = "FAGI"),
        @Type(value = DeerConfiguration.class, name = "DEER"),
        @Type(value = MetadataRegistrationConfiguration.class, name = "CATALOG")
    })
    private ToolConfiguration configuration;

    private Integer outputKey;

    protected Step() {

    }

    public Step(String name, int key, int group, EnumTool tool, EnumOperation operation, ToolConfiguration configuration) {
        this.key = key;
        this.group = group;
        this.tool = tool;
        this.operation = operation;
        this.configuration = configuration;
    }

    public Step(String name, int key, int group, EnumTool tool, EnumOperation operation, ToolConfiguration configuration, Integer outputKey) {
        this.key = key;
        this.group = group;
        this.tool = tool;
        this.operation = operation;
        this.configuration = configuration;
        this.outputKey = outputKey;
    }

    /**
     * Step unique name
     *
     * @return the step name
     */
    public String getName() {
        return name;
    }

    /**
     * The step unique key
     *
     * @return the step key
     */
    public int getKey() {
        return key;
    }


    /**
     * The step group index
     *
     * @return the group index
     */
    public int getGroup() {
        return group;
    }

    /**
     * The step operation type
     *
     * @return the operation type
     */
    public EnumOperation getOperation() {
        return operation;
    }

    /**
     * The tool that implements the operation
     *
     * @return the tool type
     */
    public EnumTool getTool() {
        return tool;
    }

    /**
     * The custom tool configuration
     *
     * @return an instance of {@link ToolConfiguration}
     */
    public ToolConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * The unique resource key of an {@link ProcessOutput} that is the output of
     * this step
     *
     * @return the output resource index
     */
    public Integer getOutputKey() {
        return outputKey;
    }


}
