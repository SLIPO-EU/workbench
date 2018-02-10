package eu.slipo.workbench.web.service;

import java.util.List;

import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.common.model.process.InvalidProcessDefinitionException;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.web.model.process.ProcessRecordView;

public interface ProcessService {

    /**
     * Validate a process definition
     *
     * @param process
     * @throws InvalidProcessDefinitionException if the given definition is invalid
     */
    void validate(ProcessDefinition definition) throws InvalidProcessDefinitionException;

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
     * Create a process by a definition
     *
     * @param definition
     * @return a list of {@link Error} objects if {@code process} properties are not
     * valid or operation has failed; Otherwise an empty array
     */
    ProcessRecord create(ProcessDefinition definition);

    /**
     * Update an existing process by providing a newer definition
     * 
     * @param id The id of the process under update
     * @param definition The newer definition
     */
    ProcessRecord update(int id, ProcessDefinition definition);
    
    /**
     * Finds all executions for a specific version of an existing process instance
     *
     * @param id the unique process id
     * @return a list of {@link ProcessExecutionRecord} objects. If no execution exists,
     * an empty list is returned
     */
    List<ProcessExecutionRecord> findExecutions(long id, long version);

}
