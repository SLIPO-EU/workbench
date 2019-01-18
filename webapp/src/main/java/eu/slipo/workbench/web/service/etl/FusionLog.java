package eu.slipo.workbench.web.service.etl;

import java.util.ArrayList;
import java.util.List;

public class FusionLog {

    public String leftURI;
    public String rightURI;
    public List<FusionAction> actions = new ArrayList<>();
    public String defaultFusionAction;
    public String validationAction;
    public String confidenceScore;

}
