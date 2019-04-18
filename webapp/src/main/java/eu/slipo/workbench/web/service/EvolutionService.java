package eu.slipo.workbench.web.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.process.EnumProcessExecutionStatus;
import eu.slipo.workbench.common.model.process.EnumStepFile;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStepFileRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStepRecord;
import eu.slipo.workbench.common.model.process.ProcessNotFoundException;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.common.model.process.Step.Input;
import eu.slipo.workbench.common.repository.ProcessRepository;
import eu.slipo.workbench.web.model.evolution.Evolution;
import eu.slipo.workbench.web.model.evolution.EvolutionSnapshot;
import eu.slipo.workbench.web.repository.FeatureRepository;

@Service
public class EvolutionService implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(EvolutionService.class);

    private final Map<UUID, String> tableColumns = new HashMap<UUID, String>();

    @Value("${vector-data.default.uri-column:uri}")
    private String defaultUriColumn;

    @Value("${vector-data.default.geometry-column:the_geom}")
    private String defaultGeometryColumn;

    @Value("${vector-data.default.geometry-simple-column:the_geom_simple}")
    private String defaultGeometrySimpleColumn;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private FeatureRepository featureRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private JdbcTemplate jdbcTemplate;

    @Override
    public void afterPropertiesSet() throws Exception {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Get evolution data for a single POI.
     *
     * If a process contains more than one output steps, it is possible to find the same
     * URI in multiple database tables. In this case the results are ambiguous; Hence we
     * do not support process definitions with multiple output steps.
     *
     * @param processId The process id
     * @param processVersion The process revision
     * @param processExecutionId The selected process execution instance
     * @param id The POI unique id
     * @param uri The POI URI
     * @return An instance of {@link Evolution} if data exists or <tt>null<tt>.
     *
     * @throws Exception If an unknown exception occurs.
     */
    public Evolution getPoiEvolution(
        long processId,
        long processVersion,
        long processExecutionId,
        long id,
        String uri
    ) throws Exception {
        List<EvolutionSnapshot> result = new ArrayList<EvolutionSnapshot>();

        // Get all revisions
        List<ProcessRecord> revisions = processRepository.getRevisions(processId, false);

        // For each revision, get the corresponding execution and find all output tables
        // that may contain the specified URI
        List<Triple<ProcessRecord, ProcessExecutionRecord, Pair<String, UUID>>> snapshots = revisions.stream()
            .map(r -> {
                // Get the execution compact view. There is only a single successful (COMPLETED)
                // execution per revision instance
                ProcessExecutionRecord e = null;
                try {
                    e = processRepository.getExecutionCompactView(r.getId(), r.getVersion());
                } catch (ProcessNotFoundException ex) {
                    // Ignore
                }
                if ((e != null) && (e.getStatus() == EnumProcessExecutionStatus.COMPLETED) && (e.isExported())) {
                    Pair<String, UUID> tables = this.getOutputStepTable(r, e);
                    return Triple.<ProcessRecord, ProcessExecutionRecord, Pair<String, UUID>>of(r, e, tables);
                }
                return null;
            })
            .filter(p -> p != null)
            .collect(Collectors.toList());

        // Search all output tables for the specified URI
        snapshots.stream()
            .forEach(s -> {
                Pair<String, UUID> table = s.getRight();
                if (table != null) {
                    final JsonNode feature = this.getFeature(table.getLeft(), table.getRight(), uri);
                    if (feature != null) {
                        final EvolutionSnapshot snapshot = new EvolutionSnapshot();
                        snapshot.process = s.getLeft();
                        snapshot.execution = s.getMiddle();
                        snapshot.stepName = table.getLeft();
                        snapshot.tableName = table.getRight();
                        snapshot.feature = feature;
                        snapshot.updates = this.featureRepository.getUpdates(table.getRight(), uri);

                        result.add(snapshot);
                    }
                }
            });

        return Evolution.of(processId, processVersion, result, id, uri);
    }

    private Pair<String, UUID> getOutputStepTable(ProcessRecord process, ProcessExecutionRecord execution) {
        // If a process contains more than one output steps, it is possible to find the
        // same URI in multiple tables. In this case the results will be ambiguous; Hence
        // we do not support process definitions with multiple output steps.

        // Find all input codes (exclude registration and transformation tasks)
        List<Input> allInput = process.getDefinition().steps()
            .stream()
            .filter(s -> s.tool() != EnumTool.REGISTER &&
                         s.tool() != EnumTool.REVERSE_TRIPLEGEO)
            .flatMap(s -> s.input().stream())
            .collect(Collectors.toList());

        // Find process output steps (all output steps whose result is not used as input). Ignore LIMES results.
        List<Pair<String, UUID>> result = process.getDefinition().steps()
            .stream()
            .filter(s -> {
                // Get output steps
                Input input = allInput.stream()
                    .filter(i -> i.inputKey().equals(s.outputKey()))
                    .findFirst()
                    .orElse(null);

                return (s.tool() != EnumTool.REGISTER &&
                        s.tool() != EnumTool.REVERSE_TRIPLEGEO &&
                        input == null);
            })
            .map(s-> {
                // Get output file with a valid table name value (not null)
                ProcessExecutionStepRecord executedStep = execution.getStep(s.key());

                List<ProcessExecutionStepFileRecord> files = executedStep.getFiles().stream()
                   .filter(f-> f.getType() == EnumStepFile.OUTPUT &&
                               f.getTableName() != null)
                   .collect(Collectors.toList());

                if ((!files.isEmpty()) && (files.size() == 1)) {
                    return Pair.<String, UUID>of(executedStep.getName(), files.get(0).getTableName());
                }
                return null;
            })
            .filter(p -> p != null)
            .collect(Collectors.toList());

        // Only single output step is supported
        return (result.size() == 1 ? result.get(0) : null);
    }

    private boolean featureExists(UUID tableName, String uri) {
        String dataQuery =
            "select count(t) " +
            "from   spatial.\"%1$s\" As t " +
            "where   %2$s = ? ";

        dataQuery = String.format(dataQuery, tableName, defaultUriColumn);

        Long count = jdbcTemplate.queryForObject(dataQuery, new Object[] { uri }, Long.class);

        return count > 0;
    }

    private JsonNode getFeature(String stepName, UUID tableName, String uri) {
        try {
            if(!this.featureExists(tableName, uri)) {
                return null;
            }

            String dataQuery =
                "select  row_to_json(f) As feature " +
                "from (";

            String columns = getColumns(tableName);

            dataQuery +=
                "    select " +
                "       'Feature' As type, " +
                "       '%5$s' As source, " +
                "       ST_AsGeoJSON(dt.%3$s)::json As geometry," +
                "       row_to_json((select columns FROM (SELECT %4$s) As columns)) As properties " +
                "    from   spatial.\"%1$s\" As dt " +
                "    where   %2$s = ? ";

            dataQuery = String.format(
                dataQuery, tableName, defaultUriColumn, defaultGeometryColumn, columns, stepName
            );

            dataQuery += ") As f ";

            String output = jdbcTemplate.queryForObject(dataQuery, new Object[] { uri }, String.class);

            return objectMapper.readTree(output);
        } catch (Exception ex) {
            logger.error("Failed to load feature", ex);
        }
        return null;
    }

    private String getColumns(UUID tableName) {
        if (tableColumns.containsKey(tableName)) {
            return tableColumns.get(tableName);
        }

        synchronized (tableColumns) {
            if (tableColumns.containsKey(tableName)) {
                return tableColumns.get(tableName);
            }

            String columnQuery = "select column_name from information_schema.columns where table_name= ?";

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(columnQuery, new Object[] { tableName.toString() });
            List<String> columns = rows.stream()
                .map(r -> (String) r.get("column_name"))
                .filter(c -> !c.equalsIgnoreCase(defaultGeometryColumn) && !c.equalsIgnoreCase(defaultGeometrySimpleColumn))
                .collect(Collectors.toList());
            String result = String.join(",", columns);

            tableColumns.put(tableName, result);
            return result;

        }
    }

}
