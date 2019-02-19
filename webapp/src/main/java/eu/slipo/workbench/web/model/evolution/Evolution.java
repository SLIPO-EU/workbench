package eu.slipo.workbench.web.model.evolution;

import java.util.List;

public class Evolution {

    public long processId;
    public long processVersion;
    public List<EvolutionSnapshot> snapshots;
    public String featureId;
    public String featureUri;

    private Evolution() {

    }

    public static Evolution of(
        long processId,
        long processVersion,
        List<EvolutionSnapshot> snapshots,
        String featureId,
        String featureUri
    ) {
        Evolution t = new Evolution();
        t.processId = processId;
        t.processVersion = processVersion;
        t.snapshots = snapshots;
        t.featureId = featureId;
        t.featureUri = featureUri;
        return t;
    }

}
