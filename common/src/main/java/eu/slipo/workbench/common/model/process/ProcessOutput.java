package eu.slipo.workbench.common.model.process;

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
    protected EnumTool tool;

    private int stepKey;

    public ProcessOutput() 
    {
        super(-1, EnumInputType.OUTPUT, null);
    }

    protected ProcessOutput(Step step, EnumResourceType resourceType)
    {
        super(step.outputKey, EnumInputType.OUTPUT, step.name, resourceType);
        this.stepKey = step.key;
        this.tool = step.tool;
    }
    
    protected static ProcessOutput fromStep(Step step)
    {
        final EnumResourceType resourceType = step.tool.getResourceType();
        return new ProcessOutput(step, resourceType);
    }
    
    /**
     * The tool that generated the output
     */
    @JsonProperty("tool")
    public EnumTool getTool() {
        return tool;
    }

    /**
     * The Key of the processing step that created this resource
     */
    @JsonProperty("stepKey")
    public int getStepKey() {
        return stepKey;
    }

}
