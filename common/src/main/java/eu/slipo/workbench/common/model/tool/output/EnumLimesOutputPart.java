package eu.slipo.workbench.common.model.tool.output;

import eu.slipo.workbench.common.model.tool.Limes;

public enum EnumLimesOutputPart implements OutputPart<Limes>
{
    ACCEPTED("accepted", EnumOutputType.OUTPUT),
    
    REVIEW("review", EnumOutputType.OUTPUT),
    
    STATS("stats", EnumOutputType.KPI),
    
    ;

    private final String key;

    private final EnumOutputType outputType;

    private EnumLimesOutputPart(String key, EnumOutputType outputType)
    {
        OutputPart.validateKey(key);
        this.key = key;
        this.outputType = outputType;
    }

    @Override
    public Class<Limes> toolType()
    {
        return Limes.class;
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

    public static EnumLimesOutputPart fromKey(String key)
    {
        return OutputPart.fromKey(key, EnumLimesOutputPart.class);
    }
}