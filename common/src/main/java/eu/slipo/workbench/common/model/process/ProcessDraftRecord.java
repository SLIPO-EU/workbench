package eu.slipo.workbench.common.model.process;

import java.time.ZonedDateTime;

import eu.slipo.workbench.common.model.user.AccountInfo;

public class ProcessDraftRecord {

    private long id;

    private AccountInfo owner;

    private ZonedDateTime updatedOn;

    private boolean template;

    private String definition;

    public ProcessDraftRecord(long id) {
        this.id = id;
    }

    public AccountInfo getOwner() {
        return owner;
    }

    public void setOwner(AccountInfo owner) {
        this.owner = owner;
    }

    public ZonedDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(ZonedDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }

    public boolean isTemplate() {
        return template;
    }

    public void setTemplate(boolean template) {
        this.template = template;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public long getId() {
        return id;
    }

}
