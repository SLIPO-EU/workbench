package eu.slipo.workbench.common.model.process;

import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;

public class TransformStep extends Step
{
    private static final long serialVersionUID = 1L;
    
    @Override
    public TriplegeoConfiguration configuration()
    {
        return (TriplegeoConfiguration) this.configuration;
    }
}
