package eu.slipo.workbench.web.service.etl;

import eu.slipo.workbench.common.model.process.EnumStepFile;
import eu.slipo.workbench.web.controller.action.ImportController.ImportResult;

/**
 *
 * Service for importing data files to PostGIS and exporting tables as GeoServer layers.
 *
 * Currently only CSV and Shape files are supported. Only a single file can be imported.
 * Multiple files must be merged before import. The service expects that a step has only a
 * single file of type {@link EnumStepFile.OUTPUT}.
 *
 */
public interface ImportService {

    /**
     * Imports a TripleGeo input file to database and publishes the data as a GeoServer
     * layer
     *
     * @param userId the current user id
     * @param executionId the process execution id
     * @param schema the database schema for the new table
     * @param geometryColumn the geometry column for the new table
     *
     * @return an instance of {@link ImportResult}
     */
    ImportResult publiseExecutionLayers(int userId, long executionId, String schema, String geometryColumn);

    /**
     * Imports a TripleGeo input file to database and publishes the data as a GeoServer
     * layer
     *
     * @param userId the current user id
     * @param executionId the process execution id
     *
     * @return an instance of {@link ImportResult}
     */
    ImportResult publiseExecutionLayers(int userId, long executionId);

}
