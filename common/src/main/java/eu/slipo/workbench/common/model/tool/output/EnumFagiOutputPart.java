package eu.slipo.workbench.common.model.tool.output;

import eu.slipo.workbench.common.model.poi.EnumTool;

public enum EnumFagiOutputPart implements OutputPart
{
    FUSED("fused", EnumOutputType.OUTPUT),
    
    REMAINING("remaining", EnumOutputType.OUTPUT),
    
    REVIEW("review", EnumOutputType.OUTPUT),
    
    STATS("stats", EnumOutputType.KPI);

    private final String key;

    private final EnumOutputType outputType;

    private EnumFagiOutputPart(String key, EnumOutputType outputType)
    {
        this.key = key;
        this.outputType = outputType;
    }
    
    @Override
    public EnumTool tool()
    {
        return EnumTool.FAGI;
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