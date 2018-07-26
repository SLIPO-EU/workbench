package eu.slipo.workbench.web.model.admin;

import java.time.ZonedDateTime;

import eu.slipo.workbench.web.model.EnumEventLevel;

/**
 * Query for searching system events
 */
public class EventQuery {

    private String userName;

    private String source;

    private EnumEventLevel level;

    private ZonedDateTime minDate;

    private ZonedDateTime maxDate;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public EnumEventLevel getLevel() {
        return level;
    }

    public void setLevel(EnumEventLevel level) {
        this.level = level;
    }

    public ZonedDateTime getMinDate() {
        return minDate;
    }

    public void setMinDate(ZonedDateTime minDate) {
        this.minDate = minDate;
    }

    public ZonedDateTime getMaxDate() {
        return maxDate;
    }

    public void setMaxDate(ZonedDateTime maxDate) {
        this.maxDate = maxDate;
    }

}
