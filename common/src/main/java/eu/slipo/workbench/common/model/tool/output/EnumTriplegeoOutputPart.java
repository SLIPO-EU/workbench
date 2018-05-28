package eu.slipo.workbench.common.model.tool.output;

import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.tool.Triplegeo;

public enum EnumTriplegeoOutputPart implements OutputPart<Triplegeo>
{
    TRANSFORMED("transformed", EnumOutputType.OUTPUT),
    
    CLASSIFICATION("classification", EnumOutputType.OUTPUT),
    
    TRANSFORMED_METADATA("transformed-metadata", EnumOutputType.KPI),
    
    CLASSIFICATION_METADATA("classification-metadata", EnumOutputType.KPI),
    
    REGISTRATION_REQUEST("registration-request", EnumOutputType.OUTPUT)
    ;

    private final String key;

    private final EnumOutputType outputType;

    private EnumTriplegeoOutputPart(String key, EnumOutputType outputType)
    {
        this.key = key;
        this.outputType = outputType;
    }

    @Override
    public EnumTool tool()
    {
        return EnumTool.TRIPLEGEO;
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

    public static EnumTriplegeoOutputPart fromKey(String key)
    {
        return OutputPart.fromKey(key, EnumTriplegeoOutputPart.class);
    }
}