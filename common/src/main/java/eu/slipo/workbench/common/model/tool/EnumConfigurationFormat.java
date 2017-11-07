package eu.slipo.workbench.common.model.tool;

/**
 * Enumeration for supported configuration formats.
 */
public enum EnumConfigurationFormat
{
    PROPERTIES("properties"),
    
    XML("xml"),
    
    JSON("json");
    
    /**
     * The default filename extension for files of this configuration format.
     */
    private final String filenameExtension;
    
    private EnumConfigurationFormat(String filenameExtension) 
    {
        this.filenameExtension = filenameExtension;
    }
    
    public String getFilenameExtension()
    {
        return filenameExtension;
    }
    
    public static EnumConfigurationFormat fromString(String name)
    {
        if (name != null && name.length() > 0) {
            for (EnumConfigurationFormat e: EnumConfigurationFormat.values())
                if (e.name().equalsIgnoreCase(name))
                    return e;
        }
        return null;
    }
}
