package eu.slipo.workbench.web.model.provenance;

import eu.slipo.workbench.common.model.poi.EnumTool;

public class EnrichOperation extends Operation {

    public String input;
    public String uri;

    private EnrichOperation() {
        super();
    }

    public static EnrichOperation of(int level, EnumTool tool, String stepName, String input, String uri) {
        EnrichOperation eo = new EnrichOperation();
        eo.level = level;
        eo.tool = tool;
        eo.stepName = stepName;
        eo.input = input;
        eo.uri = uri;
        return eo;
    }

}
