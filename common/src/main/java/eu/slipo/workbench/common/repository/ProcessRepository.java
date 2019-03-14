package eu.slipo.workbench.common.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;

import com.fasterxml.jackson.databind.JsonNode;

import eu.slipo.workbench.common.domain.ProcessExecutionEntity;
import eu.slipo.workbench.common.domain.ProcessRevisionEntity;
import eu.slipo.workbench.common.model.QueryResultPage;
import eu.slipo.workbench.common.model.poi.EnumOperation;
import eu.slipo.workbench.common.model.process.ApiCallQuery;
import eu.slipo.workbench.common.model.process.EnumProcessExecutionStatus;
import eu.slipo.workbench.common.model.process.EnumProcessTaskType;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessExecutionApiRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionNotFoundException;
import eu.slipo.workbench.common.model.process.ProcessExecutionQuery;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStepFileRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStepRecord;
import eu.slipo.workbench.common.model.process.ProcessIdentifier;
import eu.slipo.workbench.common.model.process.ProcessNotFoundException;
import eu.slipo.workbench.common.model.process.ProcessQuery;
import eu.slipo.workbench.common.model.process.ProcessRecord;

public interface ProcessRepository
{
    /**
     * A repository-level exception that represents an error to create a new execution entity
     * ({@link ProcessExecutionEntity}) when the parent entity (i.e a {@link ProcessRevisionEntity})
     * is already associated with an active (starting or running) execution.
     */
    public static class ProcessHasActiveExecutionException extends Exception
    {
        private static final long serialVersionUID = 1L;

        public ProcessHasActiveExecutionException(long id, long version)
        {
            super(String.format(
                "There is already an active (running or starting) execution for the process " +
                    "with (id,version) = (%d, %d)",
                id, version));
        }
    }

    /**
     * A repository-level exception thrown whenever an execution entity is expected to be active
     * (e.g when creating or updating a processing step inside it) but its not.
     */
    public static class ProcessExecutionNotActiveException extends Exception
    {
        private static final long serialVersionUID = 1L;

        public ProcessExecutionNotActiveException(long executionId)
        {
            super(String.format(
                "The process execution #%d was expected to be active (starting or running)",
                executionId));
        }
    }

    /**
     * Find processes filtered by a {@link ProcessQuery}
     *
     * @param query A query to filter records, or <tt>null</tt> to fetch everything
     * @param pageReq A page request
     * @param includeExecutions A flag to indicate if executions should also be returned
     */
    QueryResultPage<ProcessRecord> query(ProcessQuery query, PageRequest pageReq, boolean includeExecutions);

    default QueryResultPage<ProcessRecord> query(ProcessQuery query, PageRequest pageReq)
    {
        return query(query, pageReq, false);
    }

    /**
     * Find the most recent version of a process with a given id.
     *
     * @param id the process id
     * @return an instance of {@link ProcessRecord} if a process exists or {@code null}
     * @param includeExecutions A flag to indicate if executions should also be returned
     */
    ProcessRecord findOne(long id, boolean includeExecutions);

    default ProcessRecord findOne(long id)
    {
        return findOne(id, false);
    }

    /**
     * Find the single process with a given id and version.
     *
     * @param id The process id
     * @param version The process version
     * @param includeExecutions A flag to indicate if executions should also be returned
     * @return an instance of {@link ProcessRecord} if a process exists, or else <tt>null</tt>.
     */
    ProcessRecord findOne(long id, long version, boolean includeExecutions);

    default ProcessRecord findOne(long id, long version)
    {
        return findOne(id, version, false);
    }

    default ProcessRecord findOne(ProcessIdentifier processIdentifier, boolean includeExecutions)
    {
        return findOne(processIdentifier.getId(), processIdentifier.getVersion(), includeExecutions);
    }

    default ProcessRecord findOne(ProcessIdentifier processIdentifier)
    {
        return findOne(processIdentifier.getId(), processIdentifier.getVersion());
    }

    /**
     * Find a single process by name
     *
     * @param name The process unique name
     * @param createdBy The id of the user who created this entity
     * @return an instance of {@link ProcessRecord} if the process exists or <tt>null</tt>
     */
    ProcessRecord findOne(String name, int createdBy);

    /**
     * Map a workflow identifier to a process identifier (i.e. a pair of (id, version)
     * identifying a process revision entity).
     *
     * @param workflowId
     * @return a process identifier, or <tt>null</tt> if given workflow id is not mapped to
     *   a process revision entity.
     */
    ProcessIdentifier mapToProcessIdentifier(UUID workflowId);

    /**
     * Map a process identifier to a workflow identifier (if any).
     *
     * @param id The process id
     * @param version The process version
     * @return a workflow identifier, or <tt>null</tt> if no workflow is associated with
     *   given process revision entity.
     */
    UUID mapToWorkflowIdentifier(long id, long version);

    /**
     * @see ProcessRepository#mapToWorkflowIdentifier(long, long)
     */
    default UUID mapToWorkflowIdentifier(ProcessIdentifier processIdentifier)
    {
        return mapToWorkflowIdentifier(processIdentifier.getId(), processIdentifier.getVersion());
    }

    /**
     * Create a new process entity
     *
     * @param definition the process definition
     * @param createdBy The id of the user creating this entity
     * @param taskType the process task type
     * @param isTemplate {@code true} if process definition should be saved as a template
     *
     * @return a record for the newly created entity
     */
    ProcessRecord create(ProcessDefinition definition, int createdBy, EnumProcessTaskType taskType, boolean isTemplate);

    /**
     * Create a new process entity
     *
     * @param definition the process definition
     * @param createdBy The id of the user creating this entity
     * @param isTemplate {@code true} if process definition should be saved as a template
     *
     * @return a record for the newly created entity
     */
    ProcessRecord create(ProcessDefinition definition, int createdBy, boolean isTemplate);

    /**
     * Create a new process entity
     * @see ProcessRepository#create(ProcessDefinition, int, EnumProcessTaskType, boolean)
     */
    default ProcessRecord create(ProcessDefinition definition, int createdBy)
    {
        return create(definition, createdBy, EnumProcessTaskType.DATA_INTEGRATION, false);
    }

    /**
     * Update an existing process entity
     *
     * @param id The id of the process under update
     * @param definition the process definition
     * @param updatedBy The id of the user updating this entity
     *
     * @return a record for updated entity
     * @throws ProcessNotFoundException if given id does not correspond to a process entity
     */
    ProcessRecord update(long id, ProcessDefinition definition, int updatedBy) throws ProcessNotFoundException;

    /**
     * Find process execution record
     *
     * @param executionId the execution id (i.e the primary key of the execution)
     * @param includeNonVerifiedFiles if the result should include records for non-verified
     *   output files (i.e. not yet confirmed to exist or abandoned as part of a failed step)
     * @return an instance of {@link ProcessExecutionRecord} or <tt>null</tt> if no
     *   such execution exists
     */
    ProcessExecutionRecord findExecution(long executionId, boolean includeNonVerifiedFiles);

    /**
     * Find process execution record
     *
     * @see ProcessRepository#findExecution(long, boolean)
     */
    default ProcessExecutionRecord findExecution(long executionId)
    {
        return findExecution(executionId, false);
    }

    /**
     * Find executions for a process of given id and version
     *
     * @param id The process id
     * @param version The version of a specific revision of a process
     * @return the list of associated (to the process) execution records
     */
    List<ProcessExecutionRecord> findExecutions(long id, long version);

    /**
     * Find the latest execution for a process of given id and version. Note that if a
     * process revision has a running execution, it will always be the latest one.
     *
     * @param id The process id
     * @param version The version of a specific revision of a process
     * @return A record representing the execution entity
     */
    ProcessExecutionRecord findLatestExecution(long id, long version);

    /**
     * Get all revisions of the process with the specified id
     *
     * @param id The process id
     * @param includeExecutions A flag to indicate if executions should also be returned
     * @return A list of {@link ProcessRecord}
     */
    List<ProcessRecord> getRevisions(long id, boolean includeExecutions);

    /**
     * Get a compact view of the execution of a process of a given id and version.
     *
     * <p>A compact view of the execution is an execution comprised of the latest execution of each step,
     * successful or not (of course, a successful execution of a step will always be the latest,
     * because it will never attempt to re-execute).
     *
     * @param id The process id
     * @param version The version of a specific revision of a process
     * @return A record presenting a compact execution of given process revision
     */
    ProcessExecutionRecord getExecutionCompactView(long id, long version);

    /**
     * Find process executions filtered by a {@link ProcessExecutionQuery}.
     *
     * @param query A query to filter records, or <tt>null</tt> to fetch everything
     * @return an instance of {@link QueryResult} with {@link ProcessExecutionRecord} items
     */
    QueryResultPage<ProcessExecutionRecord> queryExecutions(ProcessExecutionQuery query, PageRequest pageReq);

    /**
     * Find process executions for API calls filtered by a {@link ApiCallQuery}.
     *
     * @param query A query to filter records, or <tt>null</tt> to fetch everything
     * @return an instance of {@link QueryResult} with {@link ProcessExecutionApiRecord} items
     */
    QueryResultPage<ProcessExecutionApiRecord> queryExecutions(ApiCallQuery query, PageRequest pageReq);

    /**
     * Create a (new) execution for a process revision with a given id and version. The new
     * execution will always be initialized with a status of {@link EnumProcessExecutionStatus#UNKNOWN}.
     *
     * @param id The process id
     * @param version
     * @param submittedBy The id of the user that submitted this execution
     * @param workflowId The id of the workflow that will carry out the actual execution
     * @return A record representing the the new execution entity
     *
     * @throws ProcessNotFoundException if given pair of (id, version) does not correspond to
     *   a process revision entity
     * @throws ProcessHasActiveExecutionException if an active execution entity is found to be
     *   associated with target process (at any time, only a single a process may be associated
     *   to at most 1 active execution).
     */
    ProcessExecutionRecord createExecution(long id, long version, int submittedBy, UUID workflowId)
        throws ProcessNotFoundException, ProcessHasActiveExecutionException;

    /**
     * Update the execution state for a process.
     *
     * <p>This method performs a shallow update of execution metadata, i.e it does not
     * examine steps or files to also update referencing entities. If instead, some step-related
     * metadata are to be persisted you should use these methods:
     * {@link ProcessRepository#createExecutionStep(long, ProcessExecutionStepRecord)} or
     * {@link ProcessRepository#updateExecutionStep(long, int, ProcessExecutionStepRecord)}
     *
     * @param executionId The execution id of a process revision
     * @param record A record holding updatable metadata. The only updatable fields are:
     *    started/completed timestamps, status and error messages.
     * @return A record representing the updated state of the execution entity
     *
     * @throws ProcessExecutionNotFoundException if given executionId does not correspond to
     *   a process execution entity
     */
    ProcessExecutionRecord updateExecution(long executionId, ProcessExecutionRecord record)
        throws ProcessExecutionNotFoundException;

    /**
     * Update the execution state for a process.
     *
     * <p>This is a convenience method using all updatable fields in an one-liner. If a timestamp
     * parameter is <tt>null</tt>, the corresponding timestamp column will not be updated.
     *
     * @param executionId The execution id of a process revision
     * @param status The execution status; if <tt>null</tt> is given, status will not be updated
     * @param started The time when the execution started; may be <tt>null</tt>
     * @param completed The time when the execution completed (successfully or not); may be <tt>null</tt>
     * @param errorMessage An optional error message; meaningful only when status is
     *   {@link EnumProcessExecutionStatus#FAILED})
     * @return A record representing the updated state of the execution entity
     *
     * @see ProcessRepository#updateExecution(long, ProcessExecutionRecord)
     */
    ProcessExecutionRecord updateExecution(
            long executionId,
            EnumProcessExecutionStatus status, ZonedDateTime started, ZonedDateTime completed, String errorMessage)
        throws ProcessExecutionNotFoundException;

    /**
     * Add a processing step to an existing execution.
     *
     * @param executionId The execution id of a process revision
     * @param record A record holding updatable metadata of a step execution
     * @return A record representing the updated state of the (parent) execution entity
     */
    ProcessExecutionRecord createExecutionStep(long executionId, ProcessExecutionStepRecord record)
        throws ProcessExecutionNotFoundException, ProcessExecutionNotActiveException;

    /**
     * Update the execution state of an existing processing step.
     *
     * @param executionId The execution id of a process revision
     * @param stepKey The step key
     * @param record A record representing the updated state of the (parent) execution entity
     * @return A record representing the updated state of the (parent) execution entity
     *
     * @throws ProcessExecutionNotFoundException if given executionId does not correspond to a
     *   a process execution entity, or if the given stepKey is invalid
     */
    ProcessExecutionRecord updateExecutionStep(long executionId, int stepKey, ProcessExecutionStepRecord record)
        throws ProcessExecutionNotFoundException, ProcessExecutionNotActiveException;

    /**
     * Update the execution state of an existing processing step by adding a new file.
     *
     * <p>This is a case of {@link ProcessRepository#updateExecutionStep(long, int, ProcessExecutionStepRecord)},
     * and is provided only as a convenience method.
     *
     * @param executionId The execution id of a process revision
     * @param stepKey The step key
     * @param record A record representing the file record to be added (i.e associated with this
     *   processing step).
     * @return
     *
     * @see ProcessRepository#updateExecutionStep(long, int, ProcessExecutionStepRecord)
     */
    ProcessExecutionRecord updateExecutionStepAddingFile(long executionId, int stepKey, ProcessExecutionStepFileRecord record)
        throws ProcessExecutionNotFoundException, ProcessExecutionNotActiveException;

    /**
     * Discard (i.e delete) an execution entity.
     *
     * @param executionId The execution id of a process revision
     * @param forceIfNotEmpty A flag to indicate if we should continue and delete the entity even
     *   if it has processing steps associated to it; if <tt>true</tt>, then all processing steps
     *   will be also deleted.
     * @return <tt>true</tt> if the execution was actually deleted
     */
    boolean discardExecution(long executionId, boolean forceIfNotEmpty)
        throws ProcessExecutionNotFoundException;

    default boolean discardExecution(long executionId) throws ProcessExecutionNotFoundException
    {
        return discardExecution(executionId, false);
    }

    /**
     * Sets a custom style for rendering a map layer for the specific process execution
     * step file.
     *
     * @param id The id of the process execution step file
     * @param style A JSON representation of the style
     */
    void setExecutionStepFileStyle(long id, JsonNode style);

    /**
     * Fix status of running executions. This is only for recovery purposes, e.g. after a non-graceful
     * shutdown that left executions and steps in an unknown state. It scans all executions (along with
     * their steps) and marks as STOPPED all that appear as UNKNOWN/RUNNING.
     */
    void clearRunningExecutions();

    /**
     * Logs the execution of an API call
     *
     * @param applicationKey The id of the application key record
     * @param execution The id of the process execution record
     * @param operation The operation type
     */
    void log(long applicationKey, long execution, EnumOperation operation);

}
