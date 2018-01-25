package eu.slipo.workbench.web.service;

import java.util.List;

import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.web.model.process.ProcessDefinitionUpdate;
import eu.slipo.workbench.web.model.process.ProcessDefinitionView;
import eu.slipo.workbench.web.model.process.ProcessExecutionRecord;

public interface IProcessService {

    /**
     * Validate a process instance
     *
     * @param process the process instance
     * @return a list of {@link Error} objects if object properties are not valid;
     * Otherwise an empty array
     */
    List<Error> validate(ProcessDefinitionUpdate process);

    /**
     * Finds the most recent version of an existing process instance
     *
     * @param id the unique process id
     * @return an instance of {@link ProcessDefinitionView} or {@code null} if no process
     * instance is found
     */
    ProcessDefinitionView findOne(long id);

    /**
     * Finds a specific version of an existing process instance
     *
     * @param id the unique process id
     * @param version the version of the process instance
     * @return an instance of {@link ProcessDefinitionView} or {@code null} if no process
     * instance is found
     */
    ProcessDefinitionView findOne(long id, long version);

    /**
     * Creates/Updates a process
     *
     * @param process the process instance
     * @return a list of {@link Error} objects if {@code process} properties are not
     * valid or operation has failed; Otherwise an empty array
     */
    List<Error> update(ProcessDefinitionUpdate process);

    /**
     * Finds all executions for a specific version of an existing process instance
     *
     * @param id the unique process id
     * @return a list of {@link ProcessExecutionRecord} objects. If no execution exists,
     * an empty list is returned
     */
    List<ProcessExecutionRecord> findExecutions(long id, long version);

}
