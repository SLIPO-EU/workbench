package eu.slipo.workbench.web.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import eu.slipo.workbench.common.model.ApplicationException;
import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.common.model.ErrorCode;
import eu.slipo.workbench.common.model.process.EnumProcessTaskType;
import eu.slipo.workbench.common.model.process.InvalidProcessDefinitionException;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessErrorCode;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStartException;
import eu.slipo.workbench.common.model.process.ProcessNotFoundException;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.common.repository.ProcessRepository;
import eu.slipo.workbench.common.service.ProcessOperator;

@Service
public class DefaultProcessService implements ProcessService {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private IAuthenticationFacade authenticationFacade;

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private ProcessOperator processOperator;

    private int currentUserId() {
        return authenticationFacade.getCurrentUserId();
    }

    private Locale currentUserLocale() {
        return authenticationFacade.getCurrentUserLocale();
    }

    private ApplicationException wrapAndFormatException(Exception ex, ErrorCode errorCode, String message) {
        return ApplicationException.fromMessage(ex, errorCode, message).withFormattedMessage(messageSource, currentUserLocale());
    }

    @Override
    public ProcessRecord create(ProcessDefinition definition) throws InvalidProcessDefinitionException {
        return this.create(definition, EnumProcessTaskType.DATA_INTEGRATION);
    }

    /**
     * Create a new process given an {@link ProcessDefinition} instance
     *
     * @param definition the process definition
     * @param taskType the process task type
     * @return an instance of {@link ProcessRecord} for the new process
     * @throws InvalidProcessDefinitionException if the given definition is invalid
     */
    @Override
    public ProcessRecord create(ProcessDefinition definition, EnumProcessTaskType taskType) throws InvalidProcessDefinitionException {

        try {
            validate(null, definition);

            return  processRepository.create(definition, currentUserId(), taskType);
        } catch(InvalidProcessDefinitionException ex) {
            throw ex;
        } catch (ApplicationException ex) {
            throw ex.withFormattedMessage(messageSource, currentUserLocale());
        } catch (Exception ex) {
            throw wrapAndFormatException(ex, ProcessErrorCode.UNKNOWN, "Failed to create process");
        }
    }

    @Override
    public ProcessRecord update(long id, ProcessDefinition definition) throws InvalidProcessDefinitionException {

        try {
            validate(id, definition);
            return processRepository.update(id, definition, currentUserId());
        } catch (InvalidProcessDefinitionException ex) {
            throw ex;
        } catch (ApplicationException ex) {
            throw ex.withFormattedMessage(messageSource, currentUserLocale());
        } catch (Exception ex) {
            throw wrapAndFormatException(ex, ProcessErrorCode.UNKNOWN, "Failed to update process");
        }
    }

    @Override
    public ProcessRecord findOne(long id) {
        return processRepository.findOne(id);
    }

    @Override
    public ProcessRecord findOne(long id, long version) {
        return processRepository.findOne(id, version);
    }

    @Override
    public List<ProcessExecutionRecord> findExecutions(long id, long version) {
        ProcessRecord record = processRepository.findOne(id, version, true);
        return record == null ? Collections.emptyList() : record.getExecutions();
    }

    @Override
    public void validate(Long id, ProcessDefinition definition) throws InvalidProcessDefinitionException {
        List<Error> errors = new ArrayList<Error>();

        // Process name must be unique
        if ((id == null) && (processRepository.findOne(definition.name()) != null)) {
            errors.add(new Error(ProcessErrorCode.NAME_DUPLICATE, "Workflow name already exists."));
        }

        if (!errors.isEmpty()) {
            throw new InvalidProcessDefinitionException(errors);
        }
    }

    @Override
    public ProcessExecutionRecord start(long id, long version) throws ProcessNotFoundException, ProcessExecutionStartException, IOException {
        return this.processOperator.start(id, version, this.authenticationFacade.getCurrentUserId());
    }

}
