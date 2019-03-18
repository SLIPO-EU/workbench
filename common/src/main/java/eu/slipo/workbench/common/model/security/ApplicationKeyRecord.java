package eu.slipo.workbench.common.model.security;

import java.io.Serializable;
import java.time.ZonedDateTime;

import eu.slipo.workbench.common.model.user.Account;

public class ApplicationKeyRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;

    private String name;

    private String key;

    private ZonedDateTime createdOn;

    private Account createdBy;

    private ZonedDateTime revokedOn;

    private Account revokedBy;

    private Account mappedAccount;

    private int maxDailyRequestLimit;

    private int maxConcurrentRequestLimit;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public ZonedDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(ZonedDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public Account getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Account createdBy) {
        this.createdBy = createdBy;
    }

    public ZonedDateTime getRevokedOn() {
        return revokedOn;
    }

    public void setRevokedOn(ZonedDateTime revokedOn) {
        this.revokedOn = revokedOn;
    }

    public Account getRevokedBy() {
        return revokedBy;
    }

    public void setRevokedBy(Account revokedBy) {
        this.revokedBy = revokedBy;
    }

    public Account getMappedAccount() {
        return mappedAccount;
    }

    public void setMappedAccount(Account mappedAccount) {
        this.mappedAccount = mappedAccount;
    }

    public int getMaxDailyRequestLimit() {
        return maxDailyRequestLimit;
    }

    public void setMaxDailyRequestLimit(int maxDailyRequestLimit) {
        this.maxDailyRequestLimit = maxDailyRequestLimit;
    }

    public int getMaxConcurrentRequestLimit() {
        return maxConcurrentRequestLimit;
    }

    public void setMaxConcurrentRequestLimit(int maxConcurrentRequestLimit) {
        this.maxConcurrentRequestLimit = maxConcurrentRequestLimit;
    }

    public boolean isRevoked() {
        return this.revokedBy != null;
    }

}
