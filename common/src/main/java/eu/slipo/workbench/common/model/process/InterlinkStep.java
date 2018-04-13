package eu.slipo.workbench.common.model.process;

import eu.slipo.workbench.common.model.tool.InterlinkConfiguration;
import eu.slipo.workbench.common.model.tool.ToolConfiguration;

public class InterlinkStep extends Step
{
    private static final long serialVersionUID = 1L;
    
    @Override
    public InterlinkConfiguration configuration()
    {
        return (InterlinkConfiguration) super.configuration();
    }
}
