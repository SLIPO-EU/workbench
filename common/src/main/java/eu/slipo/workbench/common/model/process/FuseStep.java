package eu.slipo.workbench.common.model.process;

import eu.slipo.workbench.common.model.tool.FuseConfiguration;

public class FuseStep extends Step
{
    private static final long serialVersionUID = 1L;
    
    @Override
    public FuseConfiguration configuration()
    {
        return (FuseConfiguration) super.configuration();
    }
}
