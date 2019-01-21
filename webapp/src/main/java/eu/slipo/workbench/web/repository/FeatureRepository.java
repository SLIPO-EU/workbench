package eu.slipo.workbench.web.repository;

import java.util.Map;
import java.util.UUID;

import com.vividsolutions.jts.geom.Geometry;

public interface FeatureRepository {

    /**
     * Updates a feature
     *
     * @param tableName The feature database table
     * @param id The feature id
     * @param properties The feature properties
     * @param geometry The feature geometry
     *
     * @throws Exception If a database operation fails
     */
    void update(UUID tableName, String id, Map<String, String> properties, Geometry geometry) throws Exception;

}
