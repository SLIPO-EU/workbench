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
import eu.slipo.workbench.web.model.process.ProcessDefinitionUpdate;
import eu.slipo.workbench.web.model.process.ProcessDefinitionView;
import eu.slipo.workbench.web.model.process.ProcessErrorCode;
import eu.slipo.workbench.web.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.web.model.process.ProcessRecord;
import eu.slipo.workbench.web.repository.IProcessRepository;

@Service
public class DefaultProcessService implements IProcessService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultProcessService.class);

    @Autowired
    private MessageSource messageSource;

    @Autowired
    IAuthenticationFacade authenticationFacade;

    @Autowired
    private IProcessRepository processRepository;

    @Override
    public List<Error> validate(ProcessDefinitionUpdate process) {
        return Collections.<Error>emptyList();
    }

    @Override
    public List<Error> update(ProcessDefinitionUpdate process) {
        List<Error> validationErrors = Collections.<Error>emptyList();

        try {
            validationErrors = this.validate(process);
            if (!validationErrors.isEmpty()) {
                return validationErrors;
            }

            if (process.getId() == null) {
                processRepository.create(process);
            } else {
                processRepository.update(process);
            }
        } catch (ApplicationException appEx) {
            validationErrors.add(appEx.toError(this.messageSource, authenticationFacade.getCurrentUserLocale()));
        } catch (Exception ex) {
            String message = "Failed to create process";

            logger.error(message, ex);
            validationErrors.add(new Error(ProcessErrorCode.UNKNOWN, message));
        }

        return validationErrors;
    }

    @Override
    public ProcessDefinitionView findOne(long id) {
        ProcessRecord process = processRepository.findOne(id);

        return (process == null ? null : new ProcessDefinitionView(process));
    }

    @Override
    public ProcessDefinitionView findOne(long id, long version) {
        ProcessRecord process = processRepository.findOne(id, version);

        return (process == null ? null : new ProcessDefinitionView(process));
    }

    @Override
    public List<ProcessExecutionRecord> findExecutions(long id, long version) {
        ProcessRecord process = processRepository.findOne(id, version);

        if ((process == null) || (process.getExecutions().isEmpty())) {
            Collections.<Error>emptyList();
        }

        return process.getExecutions();
    }

}
