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

    private StatisticsCollection statistics = new Dashboard.StatisticsCollection();

    private List<ResourceRecord> resources = new ArrayList<ResourceRecord>();

    private List<ProcessExecutionRecord> processes = new ArrayList<ProcessExecutionRecord>();

    private List<EventRecord> events = new ArrayList<EventRecord>();

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

    public List<EventRecord> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public void addResource(ResourceRecord r) {
        this.resources.add(r);
    }

    public void addProcessExecution(ProcessExecutionRecord e) {
        this.processes.add(e);
    }

    public void addEvent(EventRecord e) {
        this.events.add(e);
    }

    public static class StatisticsCollection {

        public ResourceStatistics resources;

        public ProcessStatistics processes;

        public EventStatistics events;

        public SystemStatistics system;

    }

    public static abstract class Statistics {

        public ZonedDateTime updatedOn = ZonedDateTime.now();

    }

    public static class ResourceStatistics extends Statistics {

        public ResourceStatistics(long total, long created, long updated) {
            super();
            this.total = total;
            this.created = created;
            this.updated = updated;
        }

        public long total;

        public long created;

        public long updated;
    }

    public static class ProcessStatistics extends Statistics {

        public ProcessStatistics(long completed, long running, long failed) {
            super();
            this.completed = completed;
            this.running = running;
            this.failed = failed;
        }

        public long completed;

        public long running;

        public long failed;
    }

    public static class EventStatistics extends Statistics {

        public EventStatistics(long error, long warning, long information) {
            super();
            this.error = error;
            this.warning = warning;
            this.information = information;
        }

        public long error;

        public long warning;

        public long information;
    }

    public static class SystemStatistics extends Statistics {

        public SystemStatistics(long usedCores, long totalCores, long usedMemory, long totalMemory, long usedDisk, long totalDisk) {
            this.usedCores = usedCores;
            this.totalCores = totalCores;
            this.usedMemory = usedMemory;
            this.totalMemory = totalMemory;
            this.usedDisk = usedDisk;
            this.totalDisk = totalDisk;
        }

        public long usedCores;

        public long totalCores;

        public long usedMemory;

        public long totalMemory;

        public long usedDisk;

        public long totalDisk;
    }

}
