package eu.slipo.workbench.web.repository;

import eu.slipo.workbench.common.model.EnumRole;
import eu.slipo.workbench.web.model.Dashboard;

public interface DashboardRepository {

    /**
     * Loads dashboard data using the default time interval for the specified user. If
     * user id value is {@code null}, all data is returned.
     *
     * @param userId the user id for filtering data
     * @param isAdmin true if the authenticated user has role {@link EnumRole#ADMIN}
     * @return an instance of {@link Dashboard}
     * @throws Exception if a data access operation has failed
     */
    Dashboard load(Integer userId) throws Exception;

    /**
     * Loads dashboard data using a time interval of {@code days} days for the specified
     * user. If user id value is {@code null}, all data is returned. The default time
     * interval in days can be set using the {@code slipo.dashboard.day-interval}
     * configuration property.
     *
     * @param userId the user id for filtering data
     * @param isAdmin true if the authenticated user has role {@link EnumRole#ADMIN}
     * @param days the time interval in days for loading recent process, resource and
     * event data
     * @return an instance of {@link Dashboard}
     * @throws Exception if a data access operation has failed
     */
    Dashboard load(Integer userId, int days) throws Exception;

}
