package eu.slipo.workbench.common.model.tool.output;

import eu.slipo.workbench.common.model.tool.ImportData;

public enum EnumImportDataOutputPart implements OutputPart<ImportData>
{
    DOWNLOAD("download");
    
    private final String key;
    
    private EnumImportDataOutputPart(String key)
    {
        OutputPart.validateKey(key);
        this.key = key; 
    }
    
    @Override
    public String key()
    {
        return key;
    }

    @Override
    public EnumOutputType outputType()
    {
        return EnumOutputType.OUTPUT;
    }

    @Override
    public Class<ImportData> toolType()
    {
        return ImportData.class;
    }
    
    public static EnumImportDataOutputPart fromKey(String key)
    {
        return OutputPart.fromKey(key, EnumImportDataOutputPart.class);
    }
}
