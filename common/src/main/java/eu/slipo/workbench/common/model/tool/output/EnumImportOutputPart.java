package eu.slipo.workbench.common.model.tool.output;

import eu.slipo.workbench.common.model.poi.EnumTool;

public enum EnumImportOutputPart implements OutputPart
{
    DOWNLOAD("download");
    
    private final String key;
    
    private EnumImportOutputPart(String key)
    {
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
    public EnumTool tool()
    {
        return EnumTool.IMPORTER;
    }
    
    public static EnumImportOutputPart fromKey(String key)
    {
        return OutputPart.fromKey(key, EnumImportOutputPart.class);
    }
}
