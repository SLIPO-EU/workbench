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
        super(-1, EnumInputType.OUTPUT, EnumResourceType.UNDEFINED, null);
    }

    protected ProcessOutput(
        int resourceKey, EnumResourceType resourceType, String name, int stepKey, EnumTool tool) 
    {
        super(resourceKey, EnumInputType.OUTPUT, resourceType, name);
        this.stepKey = stepKey;
        this.tool = tool;
    }

    /**
     * The tool that generated the output
     *
     * @return a value of {@link EnumTool}
     */
    @JsonProperty
    public EnumTool getTool() {
        return tool;
    }

    /**
     * Key of the process step that created this resource
     *
     * @return the step key
     */
    @JsonProperty
    public int getStepKey() {
        return stepKey;
    }

}
