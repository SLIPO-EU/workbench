package eu.slipo.workbench.web.service;

import java.io.IOException;
import java.util.List;

import eu.slipo.workbench.common.domain.ProcessRevisionEntity;
import eu.slipo.workbench.common.model.process.EnumProcessTaskType;
import eu.slipo.workbench.common.model.process.InvalidProcessDefinitionException;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStartException;
import eu.slipo.workbench.common.model.process.ProcessNotFoundException;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.web.model.process.ProcessRecordView;

public interface ProcessService {

    /**
     * Validate a process definition
     *
     * @id id the process unique id if an instance already exists
     * @param process the process definition
     * @throws InvalidProcessDefinitionException if the given definition is invalid
     */
    void validate(Long id, ProcessDefinition definition) throws InvalidProcessDefinitionException;

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
     * @return an instance of {@link ProcessRecord} for the new process
     * @throws InvalidProcessDefinitionException if the given definition is invalid
     */
    ProcessRecord create(ProcessDefinition definition) throws InvalidProcessDefinitionException;

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
     * Update an existing process by providing a newer definition
     *
     * @param id The id of the process under update
     * @param definition The newer definition
     * @throws InvalidProcessDefinitionException if the given definition is invalid
     */
    ProcessRecord update(long id, ProcessDefinition definition) throws InvalidProcessDefinitionException;

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
     *
     * @throws ProcessNotFoundException if no matching revision entity is found
     * @throws ProcessExecutionStartException if the execution failed to start
     * @throws IOException if an I/O error has occurred
     */
    ProcessExecutionRecord start(long id, long version) throws ProcessNotFoundException, ProcessExecutionStartException, IOException;

}
