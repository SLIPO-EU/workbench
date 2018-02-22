package eu.slipo.workbench.web.model;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.resource.ResourceRecord;


/**
 * Dashboard data
 */
public class Dashboard {

    private ZonedDateTime updatedOn = ZonedDateTime.now();

    private StatisticsCollection statistics;

    private List<ResourceRecord> resources = new ArrayList<ResourceRecord>();

    private List<ProcessExecutionRecord> processes = new ArrayList<ProcessExecutionRecord>();

    private List<Event> events = new ArrayList<Event>();

    public ZonedDateTime getUpdatedOn() {
        return updatedOn;
    }

    public StatisticsCollection getStatistics() {
        return statistics;
    }

    public List<ResourceRecord> getResources() {
        return Collections.unmodifiableList(resources);
    }

    public List<ProcessExecutionRecord> getProcesses() {
        return Collections.unmodifiableList(processes);
    }

    public List<Event> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public void addResource(ResourceRecord r) {
        this.resources.add(r);
    }

    public void addProcessExecution(ProcessExecutionRecord e) {
        this.processes.add(e);
    }

    public void addEvent(Event e) {
        this.events.add(e);
    }

    public static class StatisticsCollection {

        public ResourceStatistics resources;

        public ProcessStatistics processes;

        public EventStatistics events;

        public SystemStatistics system;

    }

    public void setStatistics(StatisticsCollection statistics) {
        this.statistics = statistics;
    }

    public static abstract class Statistics {

        public ZonedDateTime updatedOn = ZonedDateTime.now();

    }

    public static class ResourceStatistics extends Statistics {

        public ResourceStatistics(int total, int crearted, int updated) {
            super();
            this.total = total;
            this.crearted = crearted;
            this.updated = updated;
        }

        public int total;

        public int crearted;

        public int updated;
    }

    public static class ProcessStatistics extends Statistics {

        public ProcessStatistics(int completed, int running, int failed) {
            super();
            this.completed = completed;
            this.running = running;
            this.failed = failed;
        }

        public int completed;

        public int running;

        public int failed;
    }

    public static class EventStatistics extends Statistics {

        public EventStatistics(int error, int warning, int information) {
            super();
            this.error = error;
            this.warning = warning;
            this.information = information;
        }

        public int error;

        public int warning;

        public int information;
    }

    public static class SystemStatistics extends Statistics {

        public SystemStatistics(int usedCores, int totalCores, int usedMemory, int totalMemory, int usedDisk, int totalDisk) {
            this.usedCores = usedCores;
            this.totalCores = totalCores;
            this.usedMemory = usedMemory;
            this.totalMemory = totalMemory;
            this.usedDisk = usedDisk;
            this.totalDisk = totalDisk;
        }

        public int usedCores;

        public int totalCores;

        public int usedMemory;

        public int totalMemory;

        public int usedDisk;

        public int totalDisk;
    }

}
