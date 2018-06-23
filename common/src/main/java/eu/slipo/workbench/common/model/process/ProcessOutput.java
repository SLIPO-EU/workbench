package eu.slipo.workbench.common.model.process;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.slipo.workbench.common.model.poi.EnumResourceType;
import eu.slipo.workbench.common.model.poi.EnumTool;

/**
 * Output of a process step used as an input resource
 */
public class ProcessOutput extends ProcessInput
{
    private static final long serialVersionUID = 1L;

    @JsonDeserialize(using = EnumTool.Deserializer.class)
    @JsonProperty("tool")
    protected EnumTool tool;

    @JsonProperty("stepKey")
    protected int stepKey;

    protected ProcessOutput()
    {
        super(null, EnumInputType.OUTPUT, null);
    }

    protected ProcessOutput(Step step, EnumResourceType resourceType)
    {
        super(step.outputKey, EnumInputType.OUTPUT, step.name, resourceType);
        this.stepKey = step.key;
        this.tool = step.tool;
    }

    protected ProcessOutput(ProcessOutput other)
    {
        super(other.key, other.inputType, other.name, other.resourceType);
        this.stepKey = other.stepKey;
        this.tool = other.tool;
    }
    
    protected static ProcessOutput fromStep(Step step)
    {
        final EnumResourceType resourceType = step.tool.getResourceType();
        return new ProcessOutput(step, resourceType);
    }
    
    /**
     * The tool that generated the output
     */
    public EnumTool getTool() {
        return tool;
    }

    /**
     * The key of the processing step that produces this resource
     */
    @JsonIgnore
    public int stepKey()
    {
        return stepKey;
    }
}
