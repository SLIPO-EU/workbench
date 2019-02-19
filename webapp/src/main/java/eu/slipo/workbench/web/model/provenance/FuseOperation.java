package eu.slipo.workbench.web.model.provenance;

import java.util.ArrayList;
import java.util.List;

import eu.slipo.workbench.common.model.poi.EnumTool;

public class FuseOperation extends Operation {

    public Float confidenceScore;
    public List<PropertyAction> actions = new ArrayList<PropertyAction>();
    public String defaultAction;
    public String leftInput;
    public String leftUri;
    public String rightInput;
    public String rightUri;
    public String selectedUri;

    private FuseOperation() {
        super();
    }

    public static FuseOperation of(
        int level,
        EnumTool tool,
        String stepName,
        Object[] link,
        String featureUri,
        String leftInput,
        String rightInput
    ) {
        FuseOperation fo = new FuseOperation();
        fo.level = level;
        fo.tool = tool;
        fo.stepName = stepName;
        fo.leftUri = (String) link[1];
        fo.rightUri = (String) link[2];
        fo.selectedUri = fo.leftUri.equals(featureUri) ? fo.leftUri : fo.rightUri;
        fo.leftInput = leftInput;
        fo.rightInput = rightInput;
        fo.defaultAction = (String) link[3];
        fo.confidenceScore = (Float) link[4];
        return fo;
    }

}
