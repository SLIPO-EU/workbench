package eu.slipo.workbench.web.model.admin;

public class ApplicationKeyCreateRequest {

    private String applicationName;
    private int mappedAccount;
    private int maxDailyRequestLimit;
    private int maxConcurrentRequestLimit;

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public int getMappedAccount() {
        return mappedAccount;
    }

    public void setMappedAccount(int mappedAccount) {
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

}
