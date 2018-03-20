package eu.slipo.workbench.common.model;

public class Error {

    public enum EnumLevel {
        INFO,
        WARN,
        ERROR,
        ;
    }

    private ErrorCode code;

    private EnumLevel level;

    private String description;

    public Error(ErrorCode code, String description) {
        this.code = code;
        this.description = description;
        this.level = EnumLevel.ERROR;
    }

    public Error(ErrorCode code, String description, EnumLevel level) {
        this.code = code;
        this.description = description;
        this.level = level;
    }

    public ErrorCode getCode() {
        return code;
    }

    public EnumLevel getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return getDescription();
    }
}