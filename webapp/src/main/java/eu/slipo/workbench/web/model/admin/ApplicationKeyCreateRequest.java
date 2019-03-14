package eu.slipo.workbench.web.model.admin;

public class ApplicationKeyCreateRequest {

    private String applicationName;
    private Integer mappedAccount;
    private Integer maxDailyRequestLimit;
    private Integer maxConcurrentRequestLimit;

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public Integer getMappedAccount() {
        return mappedAccount;
    }

    public void setMappedAccount(Integer mappedAccount) {
        this.mappedAccount = mappedAccount;
    }

    public Integer getMaxDailyRequestLimit() {
        return maxDailyRequestLimit;
    }

    public void setMaxDailyRequestLimit(Integer maxDailyRequestLimit) {
        this.maxDailyRequestLimit = maxDailyRequestLimit;
    }

    public Integer getMaxConcurrentRequestLimit() {
        return maxConcurrentRequestLimit;
    }

    public void setMaxConcurrentRequestLimit(Integer maxConcurrentRequestLimit) {
        this.maxConcurrentRequestLimit = maxConcurrentRequestLimit;
    }

}
