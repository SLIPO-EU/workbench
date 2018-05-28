package eu.slipo.workbench.common.model.tool.output;

import eu.slipo.workbench.common.model.poi.EnumTool;

public enum EnumLimesOutputPart implements OutputPart
{
    ACCEPTED("accepted", EnumOutputType.OUTPUT),
    
    REVIEW("review", EnumOutputType.OUTPUT);

    private final String key;

    private final EnumOutputType outputType;

    private EnumLimesOutputPart(String key, EnumOutputType outputType)
    {
        this.key = key;
        this.outputType = outputType;
    }

    @Override
    public EnumTool tool()
    {
        return EnumTool.LIMES;
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