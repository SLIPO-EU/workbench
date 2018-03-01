package eu.slipo.workbench.web.repository;

import eu.slipo.workbench.web.model.Dashboard;

public interface DashboardRepository {

    /**
     * Loads dashboard data using the default time interval
     *
     * @return an instance of {@link Dashboard}
     * @throws Exception if a data access operation has failed
     */
    Dashboard load() throws Exception;

    /**
     * Loads dashboard data using a time interval of {@code days} days. The default time
     * interval in days can be set using the {@code slipo.dashboard.day-interval}
     * configuration property.
     *
     * @param days the time interval in days for loading recent process, resource and
     * event data
     * @return an instance of {@link Dashboard}
     * @throws Exception if a data access operation has failed
     */
    Dashboard load(int days) throws Exception;

}
