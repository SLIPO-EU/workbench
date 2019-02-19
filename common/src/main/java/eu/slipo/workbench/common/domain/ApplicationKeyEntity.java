package eu.slipo.workbench.common.domain;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import eu.slipo.workbench.common.model.security.ApplicationKeyRecord;

@Entity(name = "ApplicationKey")
@Table(
    schema = "public",
    name = "application_key",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_application_name",
            columnNames = { "application_name" }),
    }
)
public class ApplicationKeyEntity {

    @Id
    @Column(name = "id", updatable = false)
    @SequenceGenerator(
        sequenceName = "application_key_id_seq", name = "application_key_id_seq", initialValue = 1, allocationSize = 1)
    @GeneratedValue(generator = "application_key_id_seq", strategy = GenerationType.SEQUENCE)
    private long id = -1L;

    @NotNull
    @Column(name = "application_name", nullable = false, updatable = false)
    private String name;

    @NotNull
    @Column(name = "application_key", nullable = false, updatable = false)
    private String key;

    @NotNull
    @Column(name = "created_on", nullable = false, updatable = false)
    private ZonedDateTime createdOn = ZonedDateTime.now();

    @NotNull
    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    private AccountEntity createdBy;

    @Column(name = "revoked_on")
    private ZonedDateTime revokedOn;

    @ManyToOne
    @JoinColumn(name = "revoked_by")
    private AccountEntity revokedBy;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "mapped_account", nullable = false, updatable = false)
    private AccountEntity mappedAccount;

    @Column(name = "max_daily_request_limit")
    private int maxDailyRequestLimit;

    @Column(name = "max_concurrent_request_limit")
    private int maxConcurrentRequestLimit;

    public AccountEntity getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(AccountEntity createdBy) {
        this.createdBy = createdBy;
    }

    public ZonedDateTime getRevokedOn() {
        return revokedOn;
    }

    public void setRevokedOn(ZonedDateTime revokedOn) {
        this.revokedOn = revokedOn;
    }

    public AccountEntity getRevokedBy() {
        return revokedBy;
    }

    public void setRevokedBy(AccountEntity revokedBy) {
        this.revokedBy = revokedBy;
    }

    public AccountEntity getMappedAccount() {
        return mappedAccount;
    }

    public void setMappedAccount(AccountEntity mappedAccount) {
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

    public long getId() {
        return id;
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

    public ApplicationKeyRecord toRecord() {
        ApplicationKeyRecord record = new ApplicationKeyRecord();
        record.setCreatedBy(createdBy.toDto());
        record.setCreatedOn(createdOn);
        record.setId(id);
        record.setKey(key);
        record.setMappedAccount(mappedAccount.toDto());
        record.setMaxConcurrentRequestLimit(maxConcurrentRequestLimit);
        record.setMaxDailyRequestLimit(maxDailyRequestLimit);
        record.setName(name);
        record.setRevokedBy(revokedBy != null ? revokedBy.toDto() : null);
        record.setRevokedOn(revokedOn);
        return record;
    }

}
