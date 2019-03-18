package eu.slipo.workbench.web.model.provenance;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import eu.slipo.workbench.common.model.poi.FeatureUpdateRecord;

public class Provenance {

    public JsonNode features;
    public List<FeatureUpdateRecord> updates;
    public List<Operation> operations;
    public long featureId;
    public String featureUri;
    public String outputKey;
    public String stepName;

    private Provenance() {

    }

    public static Provenance of(
        String stepName,
        JsonNode features,
        List<Operation> operations,
        String outputKey,
        long featureId,
        String featureUri,
        List<FeatureUpdateRecord> updates
    ) {
        Provenance t = new Provenance();
        t.stepName = stepName;
        t.operations = operations;
        t.features = features;
        t.outputKey = outputKey;
        t.featureId = featureId;
        t.featureUri = featureUri;
        t.updates = updates;
        return t;
    }
}