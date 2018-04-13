package eu.slipo.workbench.common.model.process;

import eu.slipo.workbench.common.model.tool.TransformConfiguration;

public class TransformStep extends Step
{
    private static final long serialVersionUID = 1L;
    
    @Override
    public TransformConfiguration configuration()
    {
        return (TransformConfiguration) this.configuration;
    }
}
