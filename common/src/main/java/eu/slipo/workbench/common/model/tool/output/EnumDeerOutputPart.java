package eu.slipo.workbench.common.model.tool.output;

import eu.slipo.workbench.common.model.tool.Deer;

public enum EnumDeerOutputPart implements OutputPart<Deer>
{
    ENRICHED("enriched", EnumOutputType.OUTPUT), 
    
    STATS("stats", EnumOutputType.KPI);

    private final String key;

    private final EnumOutputType outputType;
    
    private EnumDeerOutputPart(String key, EnumOutputType outputType)
    {
        OutputPart.validateKey(key);
        this.key = key;
        this.outputType = outputType;
    }
    
    @Override
    public String key()
    {
        return key;
    }

    @Override
    public EnumOutputType outputType()
    {
        return outputType;
    }

    @Override
    public Class<Deer> toolType()
    {
        return Deer.class;
    }
    
    public static EnumDeerOutputPart fromKey(String key)
    {
        return OutputPart.fromKey(key, EnumDeerOutputPart.class);
    }
}
