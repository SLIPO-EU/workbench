package eu.slipo.workbench.common.repository;

import org.springframework.data.domain.PageRequest;

import eu.slipo.workbench.common.model.QueryResultPage;
import eu.slipo.workbench.common.model.resource.ResourceQuery;
import eu.slipo.workbench.common.model.resource.ResourceRecord;

public interface ResourceRepository {

    /**
     * Find resources using an instance of {@link ResourceQuery}
     *
     * @param query the query to execute
     * @param pageReq
     */
    QueryResultPage<ResourceRecord> find(ResourceQuery query, PageRequest pageReq);

    /**
     * Find a single record
     *
     * @param id the resource id
     * @return an instance of {@link ResourceRecord} if resource exists or null
     */
    ResourceRecord findOne(long id);
}
