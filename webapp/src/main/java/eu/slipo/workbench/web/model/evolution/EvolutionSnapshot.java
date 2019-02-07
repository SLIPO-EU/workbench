package eu.slipo.workbench.web.model.evolution;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

import eu.slipo.workbench.common.model.poi.FeatureUpdateRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessRecord;

public class EvolutionSnapshot {

    public ProcessRecord process;
    public ProcessExecutionRecord execution;
    public String stepName;
    public UUID tableName;
    public JsonNode feature;
    public List<FeatureUpdateRecord> updates;

}
