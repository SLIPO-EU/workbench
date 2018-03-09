package eu.slipo.workbench.common.model.process;

import eu.slipo.workbench.common.model.tool.MetadataRegistrationConfiguration;
import eu.slipo.workbench.common.model.tool.ToolConfiguration;

public class RegisterStep extends Step
{
    private static final long serialVersionUID = 1L;
    
    @Override
    public MetadataRegistrationConfiguration configuration()
    {
        return (MetadataRegistrationConfiguration) this.configuration;
    }
}
