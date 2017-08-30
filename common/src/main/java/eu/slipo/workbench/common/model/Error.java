package eu.slipo.workbench.common.model;

public class Error {

    private ErrorCode code;
    
    private String description;
    
    public Error(ErrorCode code, String description) {
        this.code = code;
        this.description = description;
    }

    public ErrorCode getCode() {
        return code;
    }

    public void setCode(ErrorCode code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString()
    {
        return getDescription();
    }
}