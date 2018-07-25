package eu.slipo.workbench.web.repository;

import org.springframework.data.domain.PageRequest;

import eu.slipo.workbench.common.model.QueryResultPage;
import eu.slipo.workbench.web.model.EventRecord;
import eu.slipo.workbench.web.model.admin.EventQuery;

public interface EventRepository {

    /**
     * Find system events filtered by a {@link EventQuery}
     *
     * @param query A query to filter records, or <tt>null</tt> to fetch everything
     * @param pageReq A page request
     */
    QueryResultPage<EventRecord> query(EventQuery query, PageRequest pageReq);

}
