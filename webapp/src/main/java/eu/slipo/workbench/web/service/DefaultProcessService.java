package eu.slipo.workbench.web.service;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import eu.slipo.workbench.common.model.ApplicationException;
import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.common.model.ErrorCode;
import eu.slipo.workbench.common.model.process.InvalidProcessDefinitionException;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessErrorCode;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.common.repository.ProcessRepository;

@Service
public class DefaultProcessService implements ProcessService 
{
    private static final Logger logger = LoggerFactory.getLogger(DefaultProcessService.class);

    @Autowired
    private MessageSource messageSource;

    @Autowired
    IAuthenticationFacade authenticationFacade;

    @Autowired
    private ProcessRepository processRepository;

    private int currentUserId()
    {
        return authenticationFacade.getCurrentUserId();
    }
    
    private Locale currentUserLocale()
    {
        return authenticationFacade.getCurrentUserLocale();
    }
    
    private ApplicationException wrapAndFormatException(Exception ex, ErrorCode errorCode, String message)
    {
        return ApplicationException.fromMessage(ex, errorCode, message)
            .withFormattedMessage(messageSource, currentUserLocale());
    }
    
    @Override
    public ProcessRecord create(ProcessDefinition definition)
    {
        try {
            validate(definition);
        } catch (InvalidProcessDefinitionException ex) {
            throw wrapAndFormatException(ex, ProcessErrorCode.INVALID, "The process definition is invalid");
        }
        
        ProcessRecord record = null;
        try {
            record = processRepository.create(definition, currentUserId());
        } catch (ApplicationException ex) {
            throw ex.withFormattedMessage(messageSource, currentUserLocale());
        } catch (Exception ex) {
            throw wrapAndFormatException(ex, ProcessErrorCode.UNKNOWN, "Failed to create process");
        }
        return record;
    }

    @Override
    public ProcessRecord update(int id, ProcessDefinition definition)
    {
        try {
            validate(definition);
        } catch (InvalidProcessDefinitionException ex) {
            throw wrapAndFormatException(ex, ProcessErrorCode.INVALID, "The process definition is invalid");
        }
        
        ProcessRecord record = null;
        try {
            record = processRepository.update(id, definition, currentUserId());
        } catch (ApplicationException ex) {
            throw ex.withFormattedMessage(messageSource, currentUserLocale());
        } catch (Exception ex) {
            throw wrapAndFormatException(ex, ProcessErrorCode.UNKNOWN, "Failed to update process");
        }
        return record;
    }
    
    @Override
    public ProcessRecord findOne(long id)
    {
        return processRepository.findOne(id);
    }

    @Override
    public ProcessRecord findOne(long id, long version)
    {
        return processRepository.findOne(id, version);
    }

    @Override
    public List<ProcessExecutionRecord> findExecutions(long id, long version)
    {
        ProcessRecord record = processRepository.findOne(id, version);
        return record == null? Collections.emptyList() : record.getExecutions();
    }

    @Override
    public void validate(ProcessDefinition definition) 
        throws InvalidProcessDefinitionException
    {
        // Todo validate(ProcessDefinition)
    }
}
