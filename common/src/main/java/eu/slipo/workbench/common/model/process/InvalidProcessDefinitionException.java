package eu.slipo.workbench.common.model.process;

import java.util.List;

import eu.slipo.workbench.common.model.Error;

/**
 * A exception that represents an invalid process definition.
 */
public class InvalidProcessDefinitionException extends Exception
{
    private static final long serialVersionUID = 1L;

    private List<Error> errors;
    
    public InvalidProcessDefinitionException(List<Error> errors)
    {
        this.errors = errors;
    }

    public List<Error> getErrors()
    {
        return errors;
    }
}
