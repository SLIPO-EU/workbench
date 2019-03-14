package eu.slipo.workbench.web.repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.vividsolutions.jts.geom.Geometry;

import eu.slipo.workbench.common.model.poi.FeatureUpdateRecord;

public interface FeatureRepository {

    /**
     * Updates a feature
     *
     * @param userId The user id
     * @param tableName The feature database table
     * @param id The feature id
     * @param properties The feature properties
     * @param geometry The feature geometry
     *
     * @throws Exception If a database operation fails
     */
    void update(Integer userId, UUID tableName, long id, Map<String, String> properties, Geometry geometry) throws Exception;

    /**
     * Get feature updates
     *
     * @param tableName The feature database table
     * @param id The feature id
     * @return an array of @{link FeatureUpdateRecord}
     */
    List<FeatureUpdateRecord> getUpdates(UUID tableName, long id);

}
