package eu.slipo.workbench.common.model.process;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.slipo.workbench.common.model.poi.EnumResourceType;
import eu.slipo.workbench.common.model.poi.EnumTool;

/**
 * Output of a process step used as an input resource
 */
public class ProcessOutput extends ProcessInput 
{
    @JsonDeserialize(using = EnumTool.Deserializer.class)
    protected EnumTool tool;

    private int stepKey;

    public ProcessOutput() {
        super();
        this.inputType = EnumInputType.OUTPUT;
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
    public EnumTool getTool() {
        return tool;
    }

    /**
     * Key of the process step that created this resource
     *
     * @return the step key
     */
    public int getStepKey() {
        return stepKey;
    }

}
