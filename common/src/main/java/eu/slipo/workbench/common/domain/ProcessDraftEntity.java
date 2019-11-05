package eu.slipo.workbench.common.domain;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import eu.slipo.workbench.common.model.process.ProcessDraftRecord;

@Entity(name = "ProcessDraft")
@Table(
    schema = "public",
    name = "process_draft",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_process_draft_owner_id", columnNames = { "owner", "id" }
        )
    }
)
public class ProcessDraftEntity {

    @Id
    @Column(name = "sid", updatable = false)
    @SequenceGenerator(
        sequenceName = "process_draft_seq", name = "process_draft_seq", initialValue = 1, allocationSize = 1
    )
    @GeneratedValue(generator = "process_draft_seq", strategy = GenerationType.SEQUENCE)
    long sid = -1L;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner", nullable = false, updatable = false)
    AccountEntity owner;

    @NotNull
    long id = 0L;

    @Version
    @Column(name = "row_version")
    long rowVersion;

    @NotNull
    @Column(name = "is_template")
    boolean isTemplate = false;

    @NotNull
    @Column(name = "updated_on")
    ZonedDateTime updatedOn;

    @NotNull
    String definition;

    public ProcessDraftEntity() {
    }

    public AccountEntity getOwner() {
        return owner;
    }

    public void setOwner(AccountEntity owner) {
        this.owner = owner;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRowVersion() {
        return rowVersion;
    }

    public boolean isTemplate() {
        return isTemplate;
    }

    public void setTemplate(boolean isTemplate) {
        this.isTemplate = isTemplate;
    }

    public ZonedDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(ZonedDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public ProcessDraftRecord toRecord() {
        ProcessDraftRecord r = new ProcessDraftRecord(id);

        r.setDefinition(definition);
        r.setOwner(owner.toAccountInfo());
        r.setTemplate(isTemplate);
        r.setUpdatedOn(updatedOn);

        return r;
    }

}
