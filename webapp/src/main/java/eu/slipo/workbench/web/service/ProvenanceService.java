package eu.slipo.workbench.web.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.util.Assert;

import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.poi.FeatureUpdateRecord;
import eu.slipo.workbench.common.model.process.EnumStepFile;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessExecutionNotFoundException;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStepFileRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStepRecord;
import eu.slipo.workbench.common.model.process.Step;
import eu.slipo.workbench.common.model.process.Step.Input;
import eu.slipo.workbench.common.model.resource.ResourceIdentifier;
import eu.slipo.workbench.common.model.resource.ResourceRecord;
import eu.slipo.workbench.common.model.tool.output.EnumDeerOutputPart;
import eu.slipo.workbench.common.model.tool.output.EnumFagiOutputPart;
import eu.slipo.workbench.common.model.tool.output.EnumTriplegeoOutputPart;
import eu.slipo.workbench.common.repository.ResourceRepository;
import eu.slipo.workbench.web.model.provenance.EnrichOperation;
import eu.slipo.workbench.web.model.provenance.FeatureQuery;
import eu.slipo.workbench.web.model.provenance.FuseOperation;
import eu.slipo.workbench.web.model.provenance.Operation;
import eu.slipo.workbench.web.model.provenance.PropertyAction;
import eu.slipo.workbench.web.model.provenance.Provenance;
import eu.slipo.workbench.web.repository.FeatureRepository;

/**
 * Service for querying POI provenance data
 */
@Service
public class ProvenanceService implements InitializingBean {

    private final static String FAGI_LINK_SUFFIX = "_links";
    private final static String FAGI_COLUMN_SUFFIX = "_actions";

    @Value("${vector-data.default.id-column:id}")
    private String defaultIdColumn;

    @Value("${vector-data.default.uri-column:uri}")
    private String defaultUriColumn;

    @Value("${vector-data.default.geometry-column:the_geom}")
    private String defaultGeometryColumn;

    @Value("${vector-data.default.geometry-simple-column:the_geom_simple}")
    private String defaultGeometrySimpleColumn;

    private final Map<UUID, String> tableColumns = new HashMap<UUID, String>();

    @PersistenceContext(unitName = "default")
    private EntityManager entityManager;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private FeatureRepository featureRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Map<String, String> attributeMapping;

    private JdbcTemplate jdbcTemplate;

    @SuppressWarnings("serial")
    @Override
    public void afterPropertiesSet() throws Exception {
        jdbcTemplate = new JdbcTemplate(dataSource);

        this.attributeMapping = new HashMap<String, String>() {{
            put("http://slipo.eu/def#address http://slipo.eu/def#number", "number");
            put("http://slipo.eu/def#address http://slipo.eu/def#postcode", "postcode");
            put("http://slipo.eu/def#phone http://slipo.eu/def#contactValue", "phone");
            put("http://slipo.eu/def#email http://slipo.eu/def#contactValue", "email");
            put("http://slipo.eu/def#name http://slipo.eu/def#nameValue", "name");
            put("http://slipo.eu/def#address http://slipo.eu/def#street", "street");
            put("http://slipo.eu/def#homepage", "homepage");
            put("http://slipo.eu/def#fax http://slipo.eu/def#contactValue", "fax");
        }};
    }

    /**
     * Get provenance data for a single POI
     *
     * @param definition The definition of the process that created the specified POI
     * @param execution The execution instance of the process
     * @param outputKey The step output key the POI belongs to
     * @param id The id of the POI
     * @param uri The URI of the POI
     * @return An instance of {@link Provenance} if data exists or <tt>null<tt>.
     *
     * @throws Exception If an unknown exception occurs.
     * @throws ProcessExecutionNotFoundException If the process execution does not exist.
     */
    public Provenance getPoiProvenance(
        ProcessDefinition definition,
        ProcessExecutionRecord execution,
        String outputKey,
        long id,
        String uri
    ) throws Exception {

        int level = 0;
        List<FeatureQuery> queries = new ArrayList<FeatureQuery>();
        List<Operation> actions = new ArrayList<Operation>();

        Step step = definition.stepByResourceKey(outputKey);

        // Search execution for inputs and operations
        this.search(level, definition, execution, outputKey, null, uri, queries, actions);

        JsonNode features = this.getFeatures(queries);

        // Get updates
        List<FeatureUpdateRecord> updates = this.featureRepository.getUpdates(queries.get(0).tableName, uri);

        return Provenance.of(step.name(), features, actions, outputKey, id, uri, updates);
    }

    protected String search(
        int level,
        ProcessDefinition definition,
        ProcessExecutionRecord execution,
        String outputKey, String partKey,
        String featureUri,
        List<FeatureQuery> queries,
        List<Operation> actions
    ) {
        // Find step for the specified output key and initialize part key if not already
        // set
        Step step = definition.stepByResourceKey(outputKey);
        if (partKey == null) {
            partKey = this.getDefaultPartKey(step.tool());
        }

        // Process every step
        ProcessExecutionStepRecord stepRecord = execution.getStep(step.key());
        ProcessExecutionStepFileRecord f = null;

        switch (step.tool()) {
            case TRIPLEGEO:
                // For TripleGeo steps, retrieve feature if a table exists
                f = stepRecord.getOutputFile(EnumStepFile.OUTPUT, partKey);
                if (f.getTableName() != null) {
                    queries.add(FeatureQuery.of(level, step.name(), f.getTableName(), featureUri));
                }
                return step.name();

            case DEER:
                Assert.equals(step.input().size(), 1);

                definition.stepByResourceKey(step.input().get(0).inputKey());

                // Input name
                String inputName = null;

                // Check if input is a resource
                Input input = step.input().get(0);
                ResourceRecord resource = this.getResource(level, definition, input, queries, featureUri);

                // If input is not a resource, search input step
                if (resource == null) {
                    // Add POI feature to the result
                    ProcessExecutionStepFileRecord fileRecord = stepRecord.getOutputFile(EnumStepFile.OUTPUT, partKey);
                    if (fileRecord.getTableName() != null) {
                        queries.add(FeatureQuery.of(level, step.name(), fileRecord.getTableName(), featureUri));
                    }

                    inputName = this.search(
                        level + 1, definition, execution, input.inputKey(), input.partKey(), featureUri, queries, actions
                    );
                }

                // Add enrichment operation to the result
                actions.add(
                    EnrichOperation.of(level, step.tool(),
                    step.name(),
                    inputName,
                    featureUri)
                );
                return step.name();

            case FAGI:
                f = stepRecord.getOutputFile(EnumStepFile.OUTPUT, partKey);
                if (f.getTableName() != null) {
                    Assert.equals(step.input().size(), 3);

                    queries.add(FeatureQuery.of(level, step.name(), f.getTableName(), featureUri));

                    // Find link if any exists
                    Object[] link = this.findLink(f.getTableName().toString() + FAGI_LINK_SUFFIX, featureUri);
                    if (link != null) {
                        // If a link exists, load actions
                        List<Triple<String, String, String>> linkActions =
                            this.findActions(f.getTableName().toString() + FAGI_COLUMN_SUFFIX, (Long) link[0]);

                        // Check if inputs are resources and add features to the result
                        Input leftInput = step.input().get(0);
                        Step leftInputStep = definition.stepByResourceKey(leftInput.inputKey());
                        ResourceRecord leftResource = this.getResource(level, definition, leftInput, queries, (String) link[1]);

                        Input rightInput = step.input().get(1);
                        Step rightInputStep = definition.stepByResourceKey(rightInput.inputKey());
                        ResourceRecord rightResource = this.getResource(level, definition, rightInput, queries, (String) link[2]);

                        // Add operation to the result
                        FuseOperation a = FuseOperation.of(
                            level,
                            step.tool(),
                            step.name(),
                            link,
                            featureUri,
                            leftInputStep != null ? leftInputStep.name() : leftResource != null ? leftResource.getName() : null,
                            rightInputStep != null ? rightInputStep.name() : rightResource != null ? rightResource.getName() : null
                        );

                        linkActions.stream().forEach(c -> {
                            a.actions.add(PropertyAction.of(c.getLeft(), c.getMiddle(), c.getRight()));
                        });

                        // Search left dataset for left URI if it is a step output
                        if (leftResource == null) {
                            this.search(
                                level + 1, definition, execution, leftInput.inputKey(), leftInput.partKey(), a.leftUri, queries, actions
                            );
                        }
                        // Search right dataset for right URI if it is a step output
                        if (rightResource == null) {
                            this.search(
                                level + 1, definition, execution, rightInput.inputKey(), rightInput.partKey(), a.rightUri, queries, actions
                            );
                        }

                        actions.add(a);

                        return step.name();
                    } else {
                        String leftInputName = null;
                        String rightInputName = null;

                        // If no link is found, search inputs for the current URI. Check if inputs are resources
                        Input leftInput = step.input().get(0);
                        Step leftInputStep = definition.stepByResourceKey(leftInput.inputKey());
                        ResourceRecord leftResource = this.getResource(level, definition, leftInput, queries, featureUri);

                        Input rightInput = step.input().get(1);
                        Step rightInputStep = definition.stepByResourceKey(rightInput.inputKey());
                        ResourceRecord rightResource = this.getResource(level, definition, rightInput, queries, featureUri);

                        // Fuse operation returned no results for the specific URI. Search both input steps
                        if (leftResource != null) {
                            leftInputName = leftResource.getName();
                        } else if (leftInputStep != null) {
                            leftInputName = this.search(
                                level + 1, definition, execution, leftInput.inputKey(), leftInput.partKey(), featureUri, queries, actions
                            );
                        }

                        if (rightResource != null) {
                            rightInputName = rightResource.getName();
                        } else if (rightInputStep != null) {
                            rightInputName = this.search(
                                level + 1, definition, execution, rightInput.inputKey(), rightInput.partKey(), featureUri, queries, actions
                            );
                        }

                        return leftInputName != null ? leftInputName : rightInputName;
                    }
                }
                // Cannot resolve provenance history
                break;

            case LIMES:
            case REVERSE_TRIPLEGEO:
            case REGISTER:
                // Do nothing
                break;

            default:
                throw new RuntimeException(String.format("Tool [%s] is not supported", step.tool().toString()));
        }

        return null;
    }

    // TODO: After upgrading to Spring Boot 2+ (and Hibernate 5.2+) update code with
    // javax.persistence.Tuple results. See https://hibernate.atlassian.net/browse/HHH-11897

    private Object[] findLink(String tableName, String featureUri) {
        String query = String.format(
            "SELECT t.* " +
            "FROM   fagi.\"%1$s\" t " +
            "WHERE  left_uri = ? or right_uri = ?",
            tableName);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query, new Object[] { featureUri, featureUri });
        return rows.stream()
            .map(r -> new Object[] {
                (Long) r.get("id"),
                (String) r.get("left_uri"),
                (String) r.get("right_uri"),
                (String) r.get("default_fusion_action"),
                StringUtils.isBlank((String) r.get("confidence_score")) ? null : Float.parseFloat((String) r.get("confidence_score"))
            })
            .findFirst().orElse(null);
    }

    private List<Triple<String, String, String>> findActions(String tableName, long parentId) {
        String query = String.format(
            "SELECT t.attribute , t.fusion_action, t.fused_value " + "FROM fagi.\"%1$s\" t " + "WHERE parent = ? ",
            tableName);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query, new Object[] { parentId });
        return rows.stream()
            .map(r -> Triple.of(this.mapAttributeName((String) r.get("attribute")), (String) r.get("fusion_action"), (String) r.get("fused_value")))
            .collect(Collectors.toList());
    }

    private ResourceRecord getResource(
        int level, ProcessDefinition definition, Input input, List<FeatureQuery> queries, String featureUri
    ) {
        ResourceIdentifier id = definition.resourceIdentifierByResourceKey(input.inputKey());
        if (id != null) {
            boolean featureFound = false;

            ResourceRecord resource = this.resourceRepository.findOne(id);
            // If the selected input is a resource, add feature to the result
            if (resource.getTableName() != null) {
                FeatureQuery query = FeatureQuery.of(
                    level, resource.getName(), resource.getTableName(), featureUri
                );

                featureFound = this.featureExists(query);
                if (featureFound) {
                    queries.add(query);
                }
            }
            return featureFound ? resource : null;
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

    private boolean featureExists(FeatureQuery query) {
        String dataQuery =
            "select count(t) " +
            "from   spatial.\"%1$s\" As t " +
            "where   %2$s = ? ";

        dataQuery = String.format(dataQuery, query.tableName, defaultUriColumn);

        Long count = jdbcTemplate.queryForObject(dataQuery, new Object[] { query.featureUri }, Long.class);

        return count > 0;
    }

    private JsonNode getFeatures(List<FeatureQuery> queries) throws IOException {
        JsonNode features = null;

        if (!queries.isEmpty()) {
            String dataQuery =
                "select row_to_json(fc) " +
                "from   ( " +
                "    select 'FeatureCollection' As type, array_to_json(array_agg(f)) As features " +
                "    from   (";

            for (int index = 0; index < queries.size(); index++) {
                FeatureQuery q = queries.get(index);

                String columns = getColumns(q.tableName);
                if(StringUtils.isBlank(columns)) {
                    continue;
                }

                dataQuery += (index == 0 ? " " : " union all ") +
                    "    select " +
                    "       'Feature' As type, " +
                    "       '%6$s' As source, " +
                    "       ST_AsGeoJSON(dt%1$s.%4$s)::json As geometry," +
                    "       row_to_json((select columns FROM (SELECT %5$s) As columns)) As properties " +
                    "    from   spatial.\"%2$s\" As dt%1$s " + "    where   %3$s = '%7$s' ";

                dataQuery = String.format(
                    dataQuery, index, q.tableName, defaultUriColumn, defaultGeometryColumn, columns, q.source, q.featureUri
                );
            }

            dataQuery += "    ) As f " + ")  As fc";

            String output = jdbcTemplate.queryForObject(dataQuery, String.class);
            features = objectMapper.readTree(output);
        }

        return features;
    }

    private String getDefaultPartKey(EnumTool tool) {
        switch (tool) {
            case TRIPLEGEO:
                return EnumTriplegeoOutputPart.TRANSFORMED.key();
            case DEER:
                return EnumDeerOutputPart.ENRICHED.key();
            case FAGI:
                return EnumFagiOutputPart.FUSED.key();
            default:
                return null;
        }
    }

    private String mapAttributeName(String value) {
        String result = attributeMapping.get(value);

        return (result == null ? value : result);
    }

}
