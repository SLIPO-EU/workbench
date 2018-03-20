package eu.slipo.workbench.web.service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import eu.slipo.workbench.common.model.ApplicationException;
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
    private IProcessValidationService processValidationService;

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
    public ProcessRecord create(ProcessDefinition definition, EnumProcessTaskType taskType) throws InvalidProcessDefinitionException {
        return create(definition, taskType, false);
    }

    @Override
    public ProcessRecord create(ProcessDefinition definition, boolean isTemplate) throws InvalidProcessDefinitionException {
        return create(definition, EnumProcessTaskType.DATA_INTEGRATION, isTemplate);
    }

    private ProcessRecord create(ProcessDefinition definition, EnumProcessTaskType taskType, boolean isTemplate) throws InvalidProcessDefinitionException {
        try {
            processValidationService.validate(null, definition, isTemplate);

            return  processRepository.create(definition, currentUserId(), taskType, isTemplate);
        } catch(InvalidProcessDefinitionException ex) {
            throw ex;
        } catch (ApplicationException ex) {
            throw ex.withFormattedMessage(messageSource, currentUserLocale());
        } catch (Exception ex) {
            throw wrapAndFormatException(ex, ProcessErrorCode.UNKNOWN, "Failed to create process");
        }
    }

    @Override
    public ProcessRecord update(long id, ProcessDefinition definition, boolean isTemplate) throws InvalidProcessDefinitionException {
        try {
            processValidationService.validate(id, definition, isTemplate);

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
    public ProcessExecutionRecord start(long id, long version) throws ProcessNotFoundException, ProcessExecutionStartException, IOException {
        return this.processOperator.start(id, version, this.authenticationFacade.getCurrentUserId());
    }

}
