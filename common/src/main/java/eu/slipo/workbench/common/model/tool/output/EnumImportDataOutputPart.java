package eu.slipo.workbench.common.model.tool.output;

import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.tool.ImportData;

public enum EnumImportDataOutputPart implements OutputPart<ImportData>
{
    DOWNLOAD("download");
    
    private final String key;
    
    private EnumImportDataOutputPart(String key)
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
    
    public static EnumImportDataOutputPart fromKey(String key)
    {
        return OutputPart.fromKey(key, EnumImportDataOutputPart.class);
    }
}
