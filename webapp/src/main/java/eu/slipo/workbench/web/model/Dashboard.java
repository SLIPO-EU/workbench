package eu.slipo.workbench.web.model;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Dashboard {

    private ZonedDateTime updatedOn = ZonedDateTime.now();

    private StatisticsCollection statistics;

    private List<Resource> resources = new ArrayList<Resource>();

    private List<Event> events = new ArrayList<Event>();

    public ZonedDateTime getUpdatedOn() {
        return updatedOn;
    }

    public StatisticsCollection getStatistics() {
        return statistics;
    }

    public List<Resource> getResources() {
        return Collections.unmodifiableList(resources);
    }

    public List<Event> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public void addResouce(Resource r) {
        this.resources.add(r);
    }

    public void addEvent(Event e) {
        this.events.add(e);
    }

    public static class StatisticsCollection {

        public ResourceStitistics resources;

        public EventStitistics events;

    }

    public void setStatistics(StatisticsCollection statistics) {
        this.statistics = statistics;
    }

    public static abstract class Statistics {

        public ZonedDateTime updatedOn = ZonedDateTime.now();

    }

    public static class ResourceStitistics extends Statistics {

        public ResourceStitistics(int total, int crearted, int updated) {
            super();
            this.total = total;
            this.crearted = crearted;
            this.updated = updated;
        }

        public int total;

        public int crearted;

        public int updated;
    }

    public static class EventStitistics extends Statistics {

        public EventStitistics(int error, int warning, int information) {
            super();
            this.error = error;
            this.warning = warning;
            this.information = information;
        }

        public int error;

        public int warning;

        public int information;
    }

}
