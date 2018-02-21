package eu.slipo.workbench.common.repository;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.PageRequest;

import eu.slipo.workbench.common.model.QueryResultPage;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessExecutionQuery;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStepFileRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStepRecord;
import eu.slipo.workbench.common.model.process.ProcessIdentifier;
import eu.slipo.workbench.common.model.process.ProcessQuery;
import eu.slipo.workbench.common.model.process.ProcessRecord;

public interface ProcessRepository 
{
    /**
     * Find processes filtered by a {@link ProcessQuery}
     * 
     * @param query A query to filter records, or <tt>null</tt> to fetch everything
     * @param pageReq A page request
     * @param includeExecutions A flag to indicate if executions should also be returned
     */
    QueryResultPage<ProcessRecord> find(ProcessQuery query, PageRequest pageReq, boolean includeExecutions);

    default QueryResultPage<ProcessRecord> find(ProcessQuery query, PageRequest pageReq)
    {
        return find(query, pageReq, false);
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
     * Todo: Lookup by pair of (name, createdBy) 
     * 
     * @param name The process unique name
     * @return an instance of {@link ProcessRecord} if the process exists or null
     */
    ProcessRecord findOne(String name);

    /**
     * Create a new process
     *
     * @param definition the process definition
     * @param createdBy The id of the user creating this entity
     * 
     * @return a view of the newly created entity
     */
    ProcessRecord create(ProcessDefinition definition, int createdBy);

    /**
     * Update an existing process
     *
     * @param id The id of the process under update
     * @param definition the process definition
     * @param updatedBy The id of the user updating this entity
     * 
     * @return a view of the updated entity
     */
    ProcessRecord update(long id, ProcessDefinition definition, int updatedBy);
    
    /**
     * Find a single process execution record
     *
     * @param executionId the execution id (i.e the primary key of the execution)
     * @return an instance of {@link ProcessExecutionRecord} or <tt>null</tt> if no
     *   such execution exists
     */
    ProcessExecutionRecord findExecution(long executionId);
    
    /**
     * Find executions for a process of given id and version
     * 
     * @param id The process id
     * @param version The version of a specific revision of a process
     * @return the list of associated (to the process) execution records
     */
    List<ProcessExecutionRecord> findExecutions(long id, long version);
    
    /**
     * Find process executions filtered by a {@link ProcessExecutionQuery}.
     *
     * @param query A query to filter records, or <tt>null</tt> to fetch everything
     * @return an instance of {@link QueryResult} with {@link ProcessExecutionRecord} items
     */
    QueryResultPage<ProcessExecutionRecord> findExecutions(ProcessExecutionQuery query, PageRequest pageReq);
    
    /**
     * Create a (new) execution for a process revision with a given id and version. 
     * 
     * @param id The process id
     * @param version
     * @param submittedBy The id of the user that submitted this execution
     * @return A record representing the state of the new execution entity
     */
    ProcessExecutionRecord createExecution(long id, long version, int submittedBy);
    
    /**
     * Update the execution state for an entire process. 
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
     */
    ProcessExecutionRecord updateExecution(long executionId, ProcessExecutionRecord record);
    
    /**
     * Add a processing step to an existing execution.
     * 
     * @param executionId The execution id of a process revision
     * @param record A record holding updatable metadata of a step execution
     * @return A record representing the updated state of the (parent) execution entity
     */
    ProcessExecutionRecord createExecutionStep(long executionId, ProcessExecutionStepRecord record);
    
    /**
     * Update the execution state of an existing processing step.
     * 
     * @param executionId The execution id of a process revision
     * @param stepKey The step key
     * @param record A record representing the updated state of the (parent) execution entity
     * @return
     */
    ProcessExecutionRecord updateExecutionStep(long executionId, int stepKey, ProcessExecutionStepRecord record);
    
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
     */
    ProcessExecutionRecord updateExecutionStepAddingFile(long executionId, int stepKey, ProcessExecutionStepFileRecord record);
}
