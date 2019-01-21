package eu.slipo.workbench.web.repository;

import java.util.List;

import eu.slipo.workbench.common.model.etl.EnumMapExportStatus;
import eu.slipo.workbench.common.model.etl.MapExportTask;

public interface MapExportTaskRepository {

    /**
     * Schedules the execution of a automatically created process for importing RDF data
     * and log files into a PostgreSQL database for the given execution id.
     *
     * @param userId The id of the user who requested the export operation.
     * @param executionId The id of the process execution.
     */
    void schedule(int userId, long executionId);

    /**
     * Sets the id of the process execution that transforms RDF data files to CSV files.
     *
     * @param taskId The map export task id.
     * @param executionId The transform process execution id
     */
    void setTransformExecution(long taskId, long executionId);

    /**
     * Sets the current status of a map export task
     *
     * @param taskId The export task id.
     * @param status The new state value.
     */
    void setStatus(long taskId, EnumMapExportStatus status);

    /**
     * Deletes a task
     *
     * @param taskId The export task id.
     */
    void remove(long taskId);

    /**
     * Get all pending map generation tasks
     *
     * @return A list of {@link MapExportTask}.
     */
    List<MapExportTask> getPendingTasks();

    /**
     * Resets status for all running tasks
     */
    void resetRunningTasks();

}
