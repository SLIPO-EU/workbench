package eu.slipo.workbench.web.service;

import eu.slipo.workbench.common.model.process.InvalidProcessDefinitionException;
import eu.slipo.workbench.common.model.process.ProcessDefinition;

public interface IProcessValidationService {

    /**
     * Validates a process definition
     *
     * @param id the process id if the process definition already exists; Otherwise
     * {@code null}.
     * @param definition the process definition
     * @param isTemplate {@code true} if the process definition is a template; Otherwise
     * {@code false}
     * @throws InvalidProcessDefinitionException if validation fails
     */
    void validate(Long id, ProcessDefinition definition, boolean isTemplate) throws InvalidProcessDefinitionException;

    /**
     * Validates a new process definition
     *
     * @param definition the process definition
     * @throws InvalidProcessDefinitionException if validation fails
     */
    void validateProcess(ProcessDefinition definition) throws InvalidProcessDefinitionException;

    /**
     * Validates an existing process definition
     *
     * @param id the process id
     * @param definition the process definition
     * @throws InvalidProcessDefinitionException if validation fails
     */
    void validateProcess(long id, ProcessDefinition definition) throws InvalidProcessDefinitionException;

    /**
     * Validates a new process definition template
     *
     * @param definition the process definition
     * @throws InvalidProcessDefinitionException if validation fails
     */
    void validateTemplate(ProcessDefinition definition) throws InvalidProcessDefinitionException;

    /**
     * Validates an existing process definition template
     *
     * @param id the process id
     * @param definition the process definition
     * @throws InvalidProcessDefinitionException if validation fails
     */
    void validateTemplate(long id, ProcessDefinition definition) throws InvalidProcessDefinitionException;

}
