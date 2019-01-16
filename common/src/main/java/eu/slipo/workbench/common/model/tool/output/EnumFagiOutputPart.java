package eu.slipo.workbench.common.model.tool.output;

import eu.slipo.workbench.common.model.tool.Fagi;

public enum EnumFagiOutputPart implements OutputPart<Fagi>
{
    FUSED("fused", EnumOutputType.OUTPUT),
    
    REMAINING("remaining", EnumOutputType.OUTPUT),
    
    REVIEW("review", EnumOutputType.OUTPUT),
    
    STATS("stats", EnumOutputType.KPI),
    
    LOG("actions", EnumOutputType.LOG);

    private final String key;

    private final EnumOutputType outputType;

    private EnumFagiOutputPart(String key, EnumOutputType outputType)
    {
        OutputPart.validateKey(key);
        this.key = key;
        this.outputType = outputType;
    }
    
    @Override
    public Class<Fagi> toolType()
    {
        return Fagi.class;
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

    public static EnumFagiOutputPart fromKey(String key)
    {
        return OutputPart.fromKey(key, EnumFagiOutputPart.class);
    }
}