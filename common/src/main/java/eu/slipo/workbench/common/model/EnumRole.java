package eu.slipo.workbench.common.model;

public enum EnumRole
{
    USER(1, "User"),
    ADMIN(2, "Site administrator"),
    AUTHOR(3, "Data integration workflow author"),
    DEVELOPER(4, "Developer role that enables additional features"),
    API(5, "SLIPO Application Programming Interface "),
    API_SESSION(5, "SLIPO Application Programming Interface session token "),
    ;

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
        for (EnumRole r: EnumRole.values()) {
            if (r.value == value) {
                return r;
            }
        }
        return null;
    }
}
