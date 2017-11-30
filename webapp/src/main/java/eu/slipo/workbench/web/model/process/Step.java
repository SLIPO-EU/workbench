package eu.slipo.workbench.web.model.process;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.slipo.workbench.web.model.EnumOperation;
import eu.slipo.workbench.web.model.EnumTool;

/**
 * A process step
 */
public class Step {

    private int index;

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

    private Integer output;

    protected Step() {

    }

    public Step(int index, EnumTool tool, EnumOperation operation, ToolConfiguration configuration) {
        this.index = index;
        this.tool = tool;
        this.operation = operation;
        this.configuration = configuration;
    }

    public Step(int index, EnumTool tool, EnumOperation operation, ToolConfiguration configuration, Integer output) {
        this.index = index;
        this.tool = tool;
        this.operation = operation;
        this.configuration = configuration;
        this.output = output;
    }

    /**
     * The step unique index
     *
     * @return the step index
     */
    public int getIndex() {
        return index;
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
     * The unique resource index of an {@link OutputProcessResource} that is the output of
     * this step
     *
     * @return the output resource index
     */
    public Integer getOutput() {
        return output;
    }

}
