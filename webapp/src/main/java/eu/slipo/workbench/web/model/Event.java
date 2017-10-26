package eu.slipo.workbench.web.model;

import java.time.ZonedDateTime;

public class Event {

    private String module;

    private ZonedDateTime createdOn;

    private String level;

    private String message;

    private String exception;

    private String clientAddress;

    private String userName;

    private String category;

    private String code;

    public Event(
            String module, ZonedDateTime createdOn, String level, String message, String exception,
            String clientAddress, String userName, String category, String code) {
        this.module = module;
        this.createdOn = createdOn;
        this.level = level;
        this.message = message;
        this.exception = exception;
        this.clientAddress = clientAddress;
        this.userName = userName;
        this.category = category;
        this.code = code;
    }

    public String getModule() {
        return module;
    }

    public ZonedDateTime getCreatedOn() {
        return createdOn;
    }

    public String getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public String getException() {
        return exception;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public String getUserName() {
        return userName;
    }

    public String getCategory() {
        return category;
    }

    public String getCode() {
        return code;
    }

}
