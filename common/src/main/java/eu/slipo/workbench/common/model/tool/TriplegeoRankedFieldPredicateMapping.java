package eu.slipo.workbench.common.model.tool;

import java.io.Serializable;

/**
 * A predicate with an assigned score
 */
public class TriplegeoRankedFieldPredicateMapping implements Serializable {

    private static final long serialVersionUID = 1L;

    private String field;
    private String predicate;
    private Double score;

    public TriplegeoRankedFieldPredicateMapping() {

    }

    public TriplegeoRankedFieldPredicateMapping(String field, String predicate, Double score) {
        this.field = field;
        this.predicate = predicate;
        this.score = score;
    }

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

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

}