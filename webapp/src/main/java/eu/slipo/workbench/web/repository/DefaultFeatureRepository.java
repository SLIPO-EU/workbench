package eu.slipo.workbench.web.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Geometry;

@Repository()
@Transactional()
public class DefaultFeatureRepository implements FeatureRepository, InitializingBean {

    @Value("${vector-data.default.schema:spatial}")
    private String defaultGeometrySchema;

    @Value("${vector-data.default.id-column:id}")
    private String defaultIdColumn;

    @Value("${vector-data.default.uri-column:uri}")
    private String defaultUriColumn;

    @Value("${vector-data.default.geometry-column:the_geom}")
    private String defaultGeometryColumn;

    @Value("${vector-data.default.geometry-simple-column:the_geom_simple}")
    private String defaultGeometrySimpleColumn;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ObjectMapper objectMapper;

    private JdbcTemplate jdbcTemplate;

    @Override
    public void afterPropertiesSet() throws Exception {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private final Map<String, List<String>> tableColumns = new HashMap<String, List<String>>();

    @Override
    public void update(
        UUID tableName, String id, Map<String, String> properties, Geometry geometry
    ) throws Exception {
        List<String> columns = this.getColumns(tableName.toString());
        if (columns.isEmpty()) {
            throw new Exception(String.format("Table %s was not found", tableName.toString()));
        }

        String sql = "update \"%1$s\".\"%2$s\" set \n";

        // Set properties
        List<Object> params = new ArrayList<Object>();
        for (String key : properties.keySet()) {
            if (columns.contains(key)) {
                if ((key.equalsIgnoreCase(this.defaultIdColumn)) ||
                    (key.equalsIgnoreCase(this.defaultUriColumn)) ||
                    (key.equalsIgnoreCase(this.defaultGeometryColumn)) ||
                    (key.equalsIgnoreCase(this.defaultGeometrySimpleColumn))) {
                    throw new Exception(String.format("Column %s is not updatable", key));
                }
                sql += key + " = ? ,\n";
                params.add(properties.get(key));
            }
        }

        // Update geometry
        String geometryAsString = this.objectMapper.writeValueAsString(geometry);

        sql += "\"%3$s\" = ST_SetSRID(ST_GeomFromGeoJSON(?), %6$s), \n";
        params.add(geometryAsString);
        sql += "\"%4$s\" = ST_SetSRID(ST_ConvexHull(ST_GeomFromGeoJSON(?)), %6$s) \n";
        params.add(geometryAsString);
        sql += "where \"%5$s\" = ?";
        params.add(id);

        sql = String.format(sql,
            this.defaultGeometrySchema,
            tableName.toString(),
            this.defaultGeometryColumn,
            this.defaultGeometrySimpleColumn,
            this.defaultIdColumn,
            "4326");

        this.jdbcTemplate.update(sql, params.toArray(new Object[params.size()]));
    }

    private List<String> getColumns(String tableName) {
        if (tableColumns.containsKey(tableName)) {
            return tableColumns.get(tableName);
        }

        synchronized (tableColumns) {
            if (tableColumns.containsKey(tableName)) {
                return tableColumns.get(tableName);
            }

            String columnQuery = String.format("select column_name from information_schema.columns where table_name='%s'", tableName);

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(columnQuery);
            List<String> columns = rows.stream()
                .map(r -> (String) r.get("column_name"))
                .filter(c -> !c.equalsIgnoreCase(defaultGeometryColumn) && !c.equalsIgnoreCase(defaultGeometrySimpleColumn))
                .collect(Collectors.toList());

            tableColumns.put(tableName, columns);

            return columns;
        }
    }

}
