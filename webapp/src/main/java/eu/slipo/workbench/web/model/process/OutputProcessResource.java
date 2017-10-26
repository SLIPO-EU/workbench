package eu.slipo.workbench.web.model.process;

public class OutputProcessResource extends ProcessResource {

    private int step;

    public OutputProcessResource() {
        super();
        this.type = EnumProcessResource.OUTPUT;
    }

    public OutputProcessResource(int index, int step) {
        super(index, EnumProcessResource.OUTPUT);
        this.step = step;
    }

    public int getStep() {
        return step;
    }

}
