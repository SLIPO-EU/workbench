package eu.slipo.workbench.web.repository;

import org.springframework.data.domain.PageRequest;

import eu.slipo.workbench.common.model.QueryResultPage;
import eu.slipo.workbench.common.model.security.ApplicationKeyRecord;
import eu.slipo.workbench.web.model.admin.ApplicationKeyQuery;

/**
 * Repository for application keys
 */
public interface ApplicationKeyRepository {

    /**
     * Default maximum concurrent request limit per application key
     */
    public static final int DEFAULT_MAX_CONCURRENT_REQUEST_LIMIT = 1;

    /**
     * Default maximum daily request limit per application key
     */
    public static final int DEFAULT_MAX_DAILY_REQUEST_LIMIT = 10;

    /**
     * Creates a new application key
     *
     * @param userId The user who created the application key
     * @param applicationName The application key name
     * @param mappedUserId The user to which the new application key is mapped
     * @return An instance of {@link ApplicationKeyRecord} if the method call was
     * successful or null
     */
    default ApplicationKeyRecord create(int userId, String applicationName, Integer mappedUserId) {
        return this.create(userId, applicationName, mappedUserId, DEFAULT_MAX_DAILY_REQUEST_LIMIT, DEFAULT_MAX_CONCURRENT_REQUEST_LIMIT);
    }

    /**
     * Creates a new application key
     *
     * @param userId The user who created the application key
     * @param applicationName The application key name
     * @param mappedUserId The user to which the new application key is mapped
     * @param maxDailyRequestLimit The maximum number of allowed daily requests
     * @param maxConcurrentRequestLimit The maximum number of allowed concurrent requests
     * @return An instance of {@link ApplicationKeyRecord} if the method call was
     * successful or null
     */
    ApplicationKeyRecord create(
        int userId, String applicationName, Integer mappedUserId, Integer maxDailyRequestLimit, Integer maxConcurrentRequestLimit
    );

    /**
     * Finds an application key by its value
     *
     * @param key The application key value
     * @return An instance of {@link ApplicationKeyRecord} if application key exists or
     * null
     */
    ApplicationKeyRecord findOne(String key);

    /**
     * Queries application keys based on the filters in {@link ApplicationKeyQuery}
     *
     * @param query The query to execute
     * @param pageRequest The data paging options for the result
     * @return A list of {@link ApplicationKeyRecord}
     */
    QueryResultPage<ApplicationKeyRecord> query(ApplicationKeyQuery query, PageRequest pageRequest);

    /**
     * Revokes an application key
     *
     * @param userId The id of the user who revoked the key
     * @param id The id of the application key to revoke
     */
    void revoke(int userId, long id);

}
