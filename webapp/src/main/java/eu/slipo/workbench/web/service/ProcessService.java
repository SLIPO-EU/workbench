package eu.slipo.workbench.web.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.PageRequest;

import eu.slipo.workbench.common.domain.ProcessRevisionEntity;
import eu.slipo.workbench.common.model.ApplicationException;
import eu.slipo.workbench.common.model.QueryResultPage;
import eu.slipo.workbench.common.model.poi.EnumOperation;
import eu.slipo.workbench.common.model.process.ApiCallQuery;
import eu.slipo.workbench.common.model.process.EnumProcessTaskType;
import eu.slipo.workbench.common.model.process.InvalidProcessDefinitionException;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessExecutionApiRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionFileNotFoundException;
import eu.slipo.workbench.common.model.process.ProcessExecutionNotFoundException;
import eu.slipo.workbench.common.model.process.ProcessExecutionQuery;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStartException;
import eu.slipo.workbench.common.model.process.ProcessExecutionStopException;
import eu.slipo.workbench.common.model.process.ProcessNotFoundException;
import eu.slipo.workbench.common.model.process.ProcessQuery;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.common.model.security.ApplicationKeyRecord;
import eu.slipo.workbench.common.model.tool.TriplegeoFieldMapping;
import eu.slipo.workbench.web.model.process.ProcessExecutionRecordView;
import eu.slipo.workbench.web.model.process.ProcessRecordView;

public interface ProcessService {

    /**
     * Find processes filtered by a {@link ProcessQuery}
     *
     * @param query a query to filter records, or {@code null} to fetch everything
     * @param pageRequest a page request
     * @return a {@link QueryResultPage} with {@link ProcessRecord} objects
     */
    QueryResultPage<ProcessRecord> find(ProcessQuery query, PageRequest pageRequest);

    /**
     * Find processes templates filtered by a {@link ProcessQuery}
     *
     * @param query a query to filter records, or {@code null} to fetch everything
     * @param pageRequest a page request
     * @return a {@link QueryResultPage} with {@link ProcessRecord} objects
     */
    QueryResultPage<ProcessRecord> findTemplates(ProcessQuery query, PageRequest pageRequest);

    /**
     * Find process executions filtered by a {@link ProcessExecutionQuery}
     *
     * @param query a query to filter records, or {@code null} to fetch everything
     * @param pageRequest a page request
     * @return a {@link QueryResultPage} with {@link ProcessExecutionRecord} objects
     */
    QueryResultPage<ProcessExecutionRecord> find(ProcessExecutionQuery query, PageRequest pageRequest);

    /**
     * Find process executions for API calls filtered by a {@link ApiCallQuery}
     *
     * @param query a query to filter records, or {@code null} to fetch everything
     * @param pageRequest a page request
     * @return a {@link QueryResultPage} with {@link ProcessExecutionApiRecord} objects
     */
    QueryResultPage<ProcessExecutionApiRecord> find(ApiCallQuery query, PageRequest pageRequest);

    /**
     * Get the compact view of the execution for a process with a specific id and version.
     * The response includes the execution steps
     *
     * @param id the process id
     * @param version the process version
     * @return an instance of {@link ProcessExecutionRecord}
     *
     * @throws ProcessNotFoundException if the process is not found
     * @throws ProcessExecutionNotFoundException if the process execution is not found
     */
    ProcessExecutionRecordView getProcessExecution(
        long id, long version
    ) throws ProcessNotFoundException, ProcessExecutionNotFoundException;

    /**
     * Get an execution for a process with a specific id and version. The response
     * includes the execution steps
     *
     * @param id the process id
     * @param version the process version
     * @param executionId the execution id
     * @param includeLogs If true, the docker log files are also returned
     * @return a list of {@link ProcessExecutionRecord}
     *
     * @throws ProcessNotFoundException if the process is not found
     * @throws ProcessExecutionNotFoundException if the process execution is not found
     */
    ProcessExecutionRecordView getProcessExecution(
        long id, long version, long executionId, boolean includeLogs
    ) throws ProcessNotFoundException, ProcessExecutionNotFoundException;

    /**
     * Get an execution for a process with a specific id and version. The response
     * includes the execution steps
     *
     * @param id the process id
     * @param version the process version
     * @param executionId the execution id
     * @return a list of {@link ProcessExecutionRecord}
     *
     * @throws ProcessNotFoundException if the process is not found
     * @throws ProcessExecutionNotFoundException if the process execution is not found
     */
    default ProcessExecutionRecordView getProcessExecution(
        long id, long version, long executionId
    ) throws ProcessNotFoundException, ProcessExecutionNotFoundException {
        return this.getProcessExecution(id, version, executionId, false);
    }

    /**
     * Finds the most recent version of an existing process instance
     *
     * @param id the unique process id
     * @return an instance of {@link ProcessRecordView} or {@code null} if no process
     * instance is found
     */
    ProcessRecord findOne(long id);

    /**
     * Finds a specific version of an existing process instance
     *
     * @param id the unique process id
     * @param version the version of the process instance
     * @return an instance of {@link ProcessRecordView} or {@code null} if no process
     * instance is found
     */
    ProcessRecord findOne(long id, long version);

    /**
     * Create a new process given an {@link ProcessDefinition} instance
     *
     * @param definition the process definition
     * @param taskType the process task type
     * @return an instance of {@link ProcessRecord} for the new process
     * @throws InvalidProcessDefinitionException if the given definition is invalid
     */
    ProcessRecord create(ProcessDefinition definition, EnumProcessTaskType taskType) throws InvalidProcessDefinitionException;

    /**
     * Create a new process given an {@link ProcessDefinition} instance
     *
     * @param definition the process definition
     * @param taskType the process task type
     * @param userId the process owner id
     * @return an instance of {@link ProcessRecord} for the new process
     * @throws InvalidProcessDefinitionException if the given definition is invalid
     */
    ProcessRecord create(ProcessDefinition definition, EnumProcessTaskType taskType, int userId) throws InvalidProcessDefinitionException;

    /**
     * Create a new process given an {@link ProcessDefinition} instance
     *
     * @param definition the process definition
     * @param isTemplate {@code true} if process definition should be saved as a template
     * @return an instance of {@link ProcessRecord} for the new process
     * @throws InvalidProcessDefinitionException if the given definition is invalid
     * @throws ApplicationException if an create operation fails
     */
    ProcessRecord create(
        ProcessDefinition definition, boolean isTemplate
    ) throws InvalidProcessDefinitionException, ApplicationException;

    /**
     * Update an existing process by providing a newer definition
     *
     * @param id The id of the process under update
     * @param definition The newer definition
     * @param isTemplate {@code true} if process definition should be saved as a template
     * @throws InvalidProcessDefinitionException if the given definition is invalid
     * @throws ApplicationException if an update operation fails
     */
    ProcessRecord update(
        long id, ProcessDefinition definition, boolean isTemplate
    ) throws InvalidProcessDefinitionException, ApplicationException;

    /**
     * Finds all executions for a specific version of an existing process instance
     *
     * @param id the unique process id
     * @return a list of {@link ProcessExecutionRecord} objects. If no execution exists,
     * an empty list is returned
     */
    List<ProcessExecutionRecord> findExecutions(long id, long version);

    /**
     * Start the execution of a process revision. A revision will be identified as the
     * {@link ProcessRevisionEntity} with the given id and version. For such an entity,
     * the application will enforce a single execution running at a given point of time.
     *
     * @param id The process id
     * @param version The version of the process (revision)
     * @param task The {@link EnumProcessTaskType} of the required operation.
     * @param userId The user that starts the process execution
     *
     * @throws ProcessNotFoundException if no matching revision entity is found
     * @throws ProcessExecutionStartException if the execution failed to start
     * @throws IOException if an I/O error has occurred
     */
    public ProcessExecutionRecord start(
        long id, long version, EnumProcessTaskType task, Integer userId
    ) throws ProcessNotFoundException, ProcessExecutionStartException, IOException;

    /**
     * Start the execution of a process revision. A revision will be identified as the
     * {@link ProcessRevisionEntity} with the given id and version. For such an entity,
     * the application will enforce a single execution running at a given point of time.
     *
     * @param id The process id
     * @param version The version of the process (revision)
     * @param task The {@link EnumProcessTaskType} of the required operation.
     *
     * @throws ProcessNotFoundException if no matching revision entity is found
     * @throws ProcessExecutionStartException if the execution failed to start
     * @throws IOException if an I/O error has occurred
     */
    ProcessExecutionRecord start(long id, long version, EnumProcessTaskType task) throws ProcessNotFoundException, ProcessExecutionStartException, IOException;

    /**
     * Stops the execution of a process revision. A revision will be identified as the
     * {@link ProcessRevisionEntity} with the given id and version. For such an entity,
     * the application will enforce a single execution running at a given point of time.
     *
     * @param id The process id
     * @param version The version of the process (revision)
     *
     * @throws ProcessNotFoundException if no matching revision entity is found
     * @throws ProcessExecutionStopException if we failed to request a stop on the given process
     */
    void stop(long id, long version) throws ProcessNotFoundException, ProcessExecutionStopException;

    /**
     * Exports process execution data to a relational database for rendering maps.
     *
     * @param id The process id
     * @param version The version of the process (revision)
     * @param executionId The unique id of the process execution instance
     *
     * @throws ProcessNotFoundException if no matching revision entity is found
     * @throws ProcessExecutionNotFoundException if no matching execution entity is found
     * @throws Exception If database update operation fails.
     */
    void exportMap(long id, long version, long executionId) throws ProcessNotFoundException, ProcessExecutionNotFoundException, Exception;

    /**
     * Exports process execution data to a relational database for rendering maps.
     *
     * @param execution The process execution instance
     *
     * @throws Exception If database update operation fails.
     */
    void exportMap(ProcessExecutionRecord execution) throws Exception;

    /**
     * Returns the file with the given {@code fileId} for the selected process revision execution.
     *
     * @param id the process id
     * @param version the process version (revision)
     * @param executionId the process execution id
     * @param fileId the id of the file to return
     * @return the {@link File} for the selected {@code fileId}
     *
     * @throws ProcessNotFoundException if the process revision is not found
     * @throws ProcessExecutionNotFoundException if the execution is not found
     * @throws ProcessExecutionFileNotFoundException if the file is not found
     */
    File getProcessExecutionFile(long id, long version, long executionId, long fileId)
        throws ProcessNotFoundException, ProcessExecutionNotFoundException, ProcessExecutionFileNotFoundException;

    /**
     * Returns the log file with the given {@code fileId} for the selected process revision execution.
     *
     * @param id the process id
     * @param version the process version (revision)
     * @param executionId the process execution id
     * @param fileId the id of the file to return
     * @return the {@link File} for the selected {@code fileId}
     *
     * @throws ProcessNotFoundException if the process revision is not found
     * @throws ProcessExecutionNotFoundException if the execution is not found
     * @throws ProcessExecutionFileNotFoundException if the file is not found
     */
    File getProcessExecutionLog(long id, long version, long executionId, long fileId)
        throws ProcessNotFoundException, ProcessExecutionNotFoundException, ProcessExecutionFileNotFoundException;

    /**
     * Get KPI data for the selected execution
     *
     * @param id the process id
     * @param version the process version
     * @param executionId the execution id
     * @param fileId the file id
     * @return a JSON object with KPI data
     *
     * @throws ApplicationException if file is not found or the file format is not supported
     * @throws ProcessExecutionNotFoundException if process execution is not found
     */
    Object getProcessExecutionKpiData(long id, long version, long executionId, long fileId)
        throws ApplicationException, ProcessExecutionNotFoundException;

    /**
     * Get log file content for the selected execution
     *
     * @param id the process id
     * @param version the process version
     * @param executionId the execution id
     * @param fileId the file id
     * @return the log file contents as plain text
     *
     * @throws ApplicationException if file is not found or the file format is not supported
     * @throws ProcessExecutionNotFoundException if process execution is not found
     */
    String getProcessExecutionLogContent(long id, long version, long executionId, long fileId)
        throws ApplicationException, ProcessExecutionNotFoundException;

    /**
     * Creates a YAML file with TripleGeo mappings from the given predicate to field
     * mappings
     *
     * @param mappings SLIPO ontology predicate to field mappings
     * @return The contents of the YAML file
     * @throws IOException if creating the file fails
     */
    String tripleGeoMappingsAsText(List<TriplegeoFieldMapping> mappings) throws IOException;

    /**
     * Logs the execution of an API call
     *
     * @param applicationKey The application key record for the key in the request header
     * @param execution The process execution
     * @param operation The operation type
     */
    void log(ApplicationKeyRecord applicationKey, ProcessExecutionRecord execution, EnumOperation operation);

}
