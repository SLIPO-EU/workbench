package eu.slipo.workbench.web.service.etl;

/**
 * Service for importing process execution data files to a relational database
 */
public interface ImportService {

    /**
     * Schedules the execution of a process instance that will import RDF data and log
     * files into a PostgreSQL database for the given process execution id.
     *
     * @param userId The id of the user who requested the export operation.
     * @param executionId The id of the process execution instance for which data must be
     * imported.
     *
     * @throws exception If an error occurs when updating the database
     */
    void schedule(int userId, long executionId) throws Exception;

}
