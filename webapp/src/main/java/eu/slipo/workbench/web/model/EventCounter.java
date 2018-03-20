package eu.slipo.workbench.web.model;

public class EventCounter {

    private EnumEventLevel level;

    private long value;

    public EventCounter(EnumEventLevel level, long value) {
        this.level = level;
        this.value = value;
    }

    public EnumEventLevel getLevel() {
        return level;
    }

    public long getValue() {
        return value;
    }

}