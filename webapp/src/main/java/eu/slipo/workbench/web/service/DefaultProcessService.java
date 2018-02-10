package eu.slipo.workbench.web.service;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import eu.slipo.workbench.common.model.ApplicationException;
import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessDefinitionView;
import eu.slipo.workbench.common.model.process.ProcessErrorCode;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.common.repository.ProcessRepository;

@Service
public class DefaultProcessService implements ProcessService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultProcessService.class);

    @Autowired
    private MessageSource messageSource;

    @Autowired
    IAuthenticationFacade authenticationFacade;

    @Autowired
    private ProcessRepository processRepository;

    @Override
    public List<Error> validate(ProcessDefinition process) {
        return Collections.<Error>emptyList();
    }

    @Override
    public List<Error> update(ProcessDefinition process) 
    {
        List<Error> validationErrors = Collections.<Error>emptyList();

        try {
            validationErrors = this.validate(process);
            if (!validationErrors.isEmpty()) {
                return validationErrors;
            }

            int userId = authenticationFacade.getCurrentUserId();
            
            if (process.getId() == null) {
                processRepository.create(process, userId);
            } else {
                processRepository.update(process, userId);
            }
        } catch (ApplicationException ex) {
            validationErrors.add(
                ex.toError(this.messageSource, authenticationFacade.getCurrentUserLocale()));
        } catch (Exception ex) {
            String message = "Failed to create process";
            logger.error(message, ex);
            validationErrors.add(new Error(ProcessErrorCode.UNKNOWN, message));
        }
        return validationErrors;
    }

    @Override
    public ProcessDefinitionView findOne(long id)
    {
        ProcessRecord process = processRepository.findOne(id);
        return (process == null ? null : new ProcessDefinitionView(process));
    }

    @Override
    public ProcessDefinitionView findOne(long id, long version)
    {
        ProcessRecord process = processRepository.findOne(id, version);
        return (process == null ? null : new ProcessDefinitionView(process));
    }

    @Override
    public List<ProcessExecutionRecord> findExecutions(long id, long version)
    {
        ProcessRecord process = processRepository.findOne(id, version);
        if ((process == null) || (process.getExecutions().isEmpty())) {
            return Collections.emptyList();
        }
        return process.getExecutions();
    }

}
