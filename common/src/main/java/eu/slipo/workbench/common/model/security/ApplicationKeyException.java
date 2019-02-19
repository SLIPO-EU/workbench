package eu.slipo.workbench.common.model.security;

public class ApplicationKeyException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final ApplicationKeyErrorCode code;

    public ApplicationKeyException(ApplicationKeyErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ApplicationKeyErrorCode getCode() {
        return code;
    }

}
