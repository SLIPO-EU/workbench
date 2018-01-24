package eu.slipo.workbench.web.repository;

import eu.slipo.workbench.web.model.QueryResult;
import eu.slipo.workbench.web.model.resource.ResourceQuery;
import eu.slipo.workbench.web.model.resource.ResourceRecord;

public interface IResourceRepository {

    /**
     * Find resources using an instance of {@link ResourceQuery}
     *
     * @param query the query to execute
     * @return an instance of {@link QueryResult} with {@link ResourceRecord} items
     */
    QueryResult<ResourceRecord> find(ResourceQuery query);

    /**
     * Find a single record
     *
     * @param id the resource id
     * @return an instance of {@link ResourceRecord} if resource exists or null
     */
    ResourceRecord findOne(long id);

}
