package eu.slipo.workbench.web.model.process;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.slipo.workbench.web.model.EnumOperation;
import eu.slipo.workbench.web.model.EnumTool;

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

    public Step() {

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

    public int getIndex() {
        return index;
    }

    public EnumOperation getOperation() {
        return operation;
    }


    public EnumTool getTool() {
        return tool;
    }

    public ToolConfiguration getConfiguration() {
        return configuration;
    }


    public Integer getOutput() {
        return output;
    }

}
