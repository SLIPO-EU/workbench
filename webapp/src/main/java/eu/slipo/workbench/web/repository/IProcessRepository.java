package eu.slipo.workbench.web.repository;

import eu.slipo.workbench.web.model.QueryResult;
import eu.slipo.workbench.web.model.process.ProcessDefinitionUpdate;
import eu.slipo.workbench.web.model.process.ProcessExecutionQuery;
import eu.slipo.workbench.web.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.web.model.process.ProcessQuery;
import eu.slipo.workbench.web.model.process.ProcessRecord;

public interface IProcessRepository {

    /**
     * Find processes using an instance of {@link ProcessQuery}
     *
     * @param query the query to execute
     * @return an instance of {@link QueryResult} with {@link ProcessRecord} items
     */
    QueryResult<ProcessRecord> find(ProcessQuery query);

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
    QueryResult<ProcessExecutionRecord> find(ProcessExecutionQuery query);

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
     * @param data the process definition
     */
    void create(ProcessDefinitionUpdate data);

    /**
     * Update an existing process
     *
     * @param data the process definition
     */
    void update(ProcessDefinitionUpdate data);

}
