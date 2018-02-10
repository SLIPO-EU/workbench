package eu.slipo.workbench.common.repository;

import org.springframework.data.domain.PageRequest;

import eu.slipo.workbench.common.model.QueryResultPage;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessExecutionQuery;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessQuery;
import eu.slipo.workbench.common.model.process.ProcessRecord;

public interface ProcessRepository 
{
    /**
     * Find processes using an instance of {@link ProcessQuery}
     * 
     * @param query the query to execute
     * @param pageReq
     */
    QueryResultPage<ProcessRecord> find(ProcessQuery query, PageRequest pageReq);

    /**
     * Find the most recent version of a single record
     *
     * @param id the process id
     * @return an instance of {@link ProcessRecord} if a process exists or {@code null}
     */
    ProcessRecord findOne(long id);

    /**
     * Find a single record
     *
     * @param id the process id
     * @param version the process version
     * @return an instance of {@link ProcessRecord} if a process exists or {@code null}
     */
    ProcessRecord findOne(long id, long version);

    /**
     * Find process executions
     *
     * @param query the query to execute
     * @return an instance of {@link QueryResult} with {@link ProcessExecutionRecord} items
     */
    QueryResultPage<ProcessExecutionRecord> find(ProcessExecutionQuery query, PageRequest pageReq);

    /**
     * Find a single process execution record
     *
     * @param id the process id
     * @param version the process version
     * @param execution the execution id
     * @return an instance of {@link ProcessExecutionRecord} or {@code null} if no
     * execution exists
     */
    ProcessExecutionRecord findOne(long id, long version, long execution);

    /**
     * Create a new process
     *
     * @param definition the process definition
     * @param userId The id of the user creating this entity
     * 
     * @return a view of the newly created entity
     */
    ProcessRecord create(ProcessDefinition definition, int userId);

    /**
     * Update an existing process
     *
     * @param id The id of the process under update
     * @param definition the process definition
     * @param userId The id of the user updating this entity
     * 
     * @return a view of the updated entity
     */
    ProcessRecord update(long id, ProcessDefinition definition, int userId);

}
