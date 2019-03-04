package eu.slipo.workbench.common.model.tool;

import java.io.Serializable;

/**
 * A field to predicate mapping
 */
public class TriplegeoFieldMapping implements Serializable {

    private static final long serialVersionUID = 1L;

    private String field;
    private String predicate;
    private String type;
    private String language;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

}