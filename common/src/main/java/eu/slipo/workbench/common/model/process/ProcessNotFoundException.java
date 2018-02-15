package eu.slipo.workbench.common.model.process;

public class ProcessNotFoundException extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public ProcessNotFoundException(long id)
    {
        super(String.format("No such process (id=%d)", id));
    }
    
    public ProcessNotFoundException(long id, long version)
    {
        super(String.format("No such process (id=%d@%d)", id, version));
    }
}
