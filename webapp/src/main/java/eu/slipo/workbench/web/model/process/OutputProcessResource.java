package eu.slipo.workbench.web.model.process;

/**
 * Output of a process step used as an input resource
 */
public class OutputProcessResource extends ProcessResource {

    private int stepIndex;

    public OutputProcessResource() {
        super();
        this.type = EnumProcessResource.OUTPUT;
    }

    public OutputProcessResource(int resourceIndex, int stepIndex) {
        super(resourceIndex, EnumProcessResource.OUTPUT);
        this.stepIndex = stepIndex;
    }

    /**
     * Index of the process step that created this resource
     *
     * @return the step index
     */
    public int getStepIndex() {
        return stepIndex;
    }

}
