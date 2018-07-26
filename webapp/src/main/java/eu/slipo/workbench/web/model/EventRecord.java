package eu.slipo.workbench.web.model;

import java.time.ZonedDateTime;

/**
 * Event log data
 */
public class EventRecord {

    private long id;

    private String module;

    private ZonedDateTime createdOn;

    private EnumEventLevel level;

    private String message;

    private String exception;

    private String clientAddress;

    private String userName;

    private String category;

    private String code;

    public EventRecord(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public ZonedDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(ZonedDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public EnumEventLevel getLevel() {
        return level;
    }

    public void setLevel(EnumEventLevel level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

}
