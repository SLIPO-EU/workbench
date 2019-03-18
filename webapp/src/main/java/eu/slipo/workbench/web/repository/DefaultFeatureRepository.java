package eu.slipo.workbench.web.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Geometry;

import eu.slipo.workbench.common.domain.FeatureUpdateEntity;
import eu.slipo.workbench.common.model.poi.FeatureUpdateRecord;

@Repository()
@Transactional()
public class DefaultFeatureRepository implements FeatureRepository {

    @Value("${vector-data.default.schema:spatial}")
    private String defaultGeometrySchema;

    @Value("${vector-data.default.id-column:id}")
    private String defaultIdColumn;

    @Value("${vector-data.default.surrogate-id-column:__index}")
    private String defaultSurrogateIdColumn;

    @Value("${vector-data.default.uri-column:uri}")
    private String defaultUriColumn;

    @Value("${vector-data.default.geometry-column:the_geom}")
    private String defaultGeometryColumn;

    @Value("${vector-data.default.geometry-simple-column:the_geom_simple}")
    private String defaultGeometrySimpleColumn;

    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private JdbcTemplate jdbcTemplate;

    private final Map<String, List<String>> tableColumns = new HashMap<String, List<String>>();

    @Override
    public void update(
        Integer userId, UUID tableName, long id, Map<String, String> properties, Geometry geometry
    ) throws Exception {
        List<String> columns = this.getColumns(tableName.toString());
        if (columns.isEmpty()) {
            throw new Exception(String.format("Table %s was not found", tableName.toString()));
        }

        // Get current values
        String query = String.format(
            "SELECT t.* " +
            "FROM   \"%1$s\".\"%2$s\" t " +
            "WHERE  %3$s = ?",
            this.defaultGeometrySchema, tableName.toString(), this.defaultSurrogateIdColumn
        );

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query, new Object[] { id });
        Map<String, String> current = new HashMap<String, String>();
        for (String key : properties.keySet()) {
            if (columns.contains(key) && this.isUpdatable(key)) {
                current.put(key, (String) rows.get(0).get(key));
            }
        }

        // Insert history record
        List<Object> insertParams = new ArrayList<Object>();

        String insert = String.format(
            "insert into public.feature_update_history ( " +
            "table_name, feature_id, properties, the_geom, the_geom_simple, updated_on, updated_by) " +
            "select ?, ?, ?, the_geom, the_geom_simple, now(), ? " +
            "from \"%1$s\".\"%2$s\" " +
            "where %3$s = ?",
            this.defaultGeometrySchema, tableName.toString(), this.defaultSurrogateIdColumn);

        insertParams.add(tableName);
        insertParams.add(id);
        insertParams.add(this.objectMapper.writeValueAsString(current));
        insertParams.add(userId);
        insertParams.add(id);

        this.jdbcTemplate.update(insert, insertParams.toArray(new Object[insertParams.size()]));

        // Update record
        List<Object> updateParams = new ArrayList<Object>();

        String update = "update \"%1$s\".\"%2$s\" set \n";
        for (String key : properties.keySet()) {
            if (columns.contains(key) && this.isUpdatable(key)) {
                update += (updateParams.isEmpty() ? "" : ",") + key + " = ? \n";
                updateParams.add(properties.get(key));
            }
        }

        if (geometry != null) {
            String updateGeometryAsString = this.objectMapper.writeValueAsString(geometry);

            if (!updateParams.isEmpty()) {
                update += ",";
            }
            update += "\"%3$s\" = ST_SetSRID(ST_GeomFromGeoJSON(?), %6$s), \n";
            updateParams.add(updateGeometryAsString);
            update += "\"%4$s\" = ST_SetSRID(ST_ConvexHull(ST_GeomFromGeoJSON(?)), %6$s) \n";
            updateParams.add(updateGeometryAsString);
        }

        update += "where \"%5$s\" = ?";
        updateParams.add(id);

        update = String.format(update,
            this.defaultGeometrySchema,
            tableName.toString(),
            this.defaultGeometryColumn,
            this.defaultGeometrySimpleColumn,
            this.defaultSurrogateIdColumn,
            "4326");

        this.jdbcTemplate.update(update, updateParams.toArray(new Object[updateParams.size()]));
    }

    @Override
    public List<FeatureUpdateRecord> getUpdates(UUID tableName, long id) {
        String qlString = "FROM FeatureUpdate u WHERE u.tableName = :tableName and u.featureId = :id order by u.id";

        return entityManager
            .createQuery(qlString, FeatureUpdateEntity.class)
            .setParameter("tableName", tableName)
            .setParameter("id", id)
            .getResultList()
            .stream()
            .map(e -> e.toRecord())
            .collect(Collectors.toList());
    }

    private List<String> getColumns(String tableName) {
        if (tableColumns.containsKey(tableName)) {
            return tableColumns.get(tableName);
        }

        synchronized (tableColumns) {
            if (tableColumns.containsKey(tableName)) {
                return tableColumns.get(tableName);
            }

            String columnQuery = "select column_name from information_schema.columns where table_name = ?";

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(columnQuery, new Object[] { tableName });
            List<String> columns = rows.stream()
                .map(r -> (String) r.get("column_name"))
                .filter(c -> !c.equalsIgnoreCase(defaultGeometryColumn) && !c.equalsIgnoreCase(defaultGeometrySimpleColumn))
                .collect(Collectors.toList());

            tableColumns.put(tableName, columns);

            return columns;
        }
    }

    private boolean isUpdatable(String column) {
        return (!column.equalsIgnoreCase(this.defaultIdColumn)) &&
               (!column.equalsIgnoreCase(this.defaultUriColumn)) &&
               (!column.equalsIgnoreCase(this.defaultGeometryColumn)) &&
               (!column.equalsIgnoreCase(this.defaultGeometrySimpleColumn));
    }

}
