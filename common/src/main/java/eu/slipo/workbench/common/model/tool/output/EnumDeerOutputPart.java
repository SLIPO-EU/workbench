package eu.slipo.workbench.common.model.tool.output;

import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.tool.Deer;

public enum EnumDeerOutputPart implements OutputPart<Deer>
{
    ENRICHED("enriched", EnumOutputType.OUTPUT);

    private final String key;

    private final EnumOutputType outputType;
    
    private EnumDeerOutputPart(String key, EnumOutputType outputType)
    {
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
    public EnumTool tool()
    {
        return EnumTool.DEER;
    }
    
    public static EnumDeerOutputPart fromKey(String key)
    {
        return OutputPart.fromKey(key, EnumDeerOutputPart.class);
    }
}
