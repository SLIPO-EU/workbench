package eu.slipo.workbench.model;

public enum EnumRole
{
    USER(1, "user"),
    ADMIN(2, "site administrator"),
    MAINTAINER(3, "site maintainer");
    
    private final int value;
    
    private final String description;
    
    private EnumRole(int value, String description)
    {
        this.value = value;
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }

    public int getValue()
    {
        return value;
    }
    
    public static EnumRole valueOf(int value)
    {
        for (EnumRole r: EnumRole.values())
            if (r.value == value)
                return r;
        return null;
    }
}
