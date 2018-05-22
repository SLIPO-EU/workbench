package eu.slipo.workbench.web.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.common.model.process.InvalidProcessDefinitionException;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessErrorCode;
import eu.slipo.workbench.common.repository.ProcessRepository;

@Service
public class DefaultProcessValidationService implements IProcessValidationService {

    @Autowired
    private IAuthenticationFacade authenticationFacade;

    @Autowired
    private ProcessRepository processRepository;

    private int currentUserId() {
        return authenticationFacade.getCurrentUserId();
    }

    @Override
    public void validate(Long id, ProcessDefinition definition, boolean isTemplate) throws InvalidProcessDefinitionException {

        List<Error> errors = new ArrayList<Error>();

        // Process name must be unique
        if ((id == null) && (processRepository.findOne(definition.name(), currentUserId()) != null)) {
            errors.add(new Error(ProcessErrorCode.NAME_DUPLICATE, "Workflow name already exists."));
        }

        if (!errors.isEmpty()) {
            throw new InvalidProcessDefinitionException(errors);
        }
    }

    @Override
    public void validateProcess(ProcessDefinition definition) throws InvalidProcessDefinitionException {
        validate(null, definition, false);
    }

    @Override
    public void validateProcess(long id, ProcessDefinition definition) throws InvalidProcessDefinitionException {
        validate(id, definition, false);
    }

    @Override
    public void validateTemplate(ProcessDefinition definition) throws InvalidProcessDefinitionException {
        validate(null, definition, true);
    }

    @Override
    public void validateTemplate(long id, ProcessDefinition definition) throws InvalidProcessDefinitionException {
        validate(id, definition, true);
    }

}
