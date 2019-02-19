package eu.slipo.workbench.web.model.provenance;

import java.util.UUID;

public class FeatureQuery {

    public int level;
    public String featureUri;
    public String selectedUri;
    public String source;
    public UUID tableName;

    private FeatureQuery() {

    }

    public static FeatureQuery of(int level, String source, UUID tableName, String featureUri) {
        FeatureQuery fq = new FeatureQuery();
        fq.level = level;
        fq.source = source;
        fq.tableName = tableName;
        fq.featureUri = featureUri;
        return fq;
    }

}
