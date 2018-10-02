package eu.slipo.workbench.common.model.tool.output;

import eu.slipo.workbench.common.model.tool.ReverseTriplegeo;

public enum EnumReverseTriplegeoOutputPart implements OutputPart<ReverseTriplegeo>
{
    TRANSFORMED("transformed", EnumOutputType.OUTPUT),

    ;

    private final String key;

    private final EnumOutputType outputType;

    private EnumReverseTriplegeoOutputPart(String key, EnumOutputType outputType)
    {
        OutputPart.validateKey(key);
        this.key = key;
        this.outputType = outputType;
    }

    @Override
    public Class<ReverseTriplegeo> toolType()
    {
        return ReverseTriplegeo.class;
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

    public static EnumReverseTriplegeoOutputPart fromKey(String key)
    {
        return OutputPart.fromKey(key, EnumReverseTriplegeoOutputPart.class);
    }
}
