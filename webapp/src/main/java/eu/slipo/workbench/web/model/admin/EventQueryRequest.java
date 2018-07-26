package eu.slipo.workbench.web.model.admin;

import org.springframework.data.domain.PageRequest;

import eu.slipo.workbench.web.model.QueryPagingOptions;

public class EventQueryRequest {

    private QueryPagingOptions pagingOptions;

    private EventQuery query;

    public QueryPagingOptions getPagingOptions() {
        return pagingOptions;
    }

    public PageRequest getPageRequest() {
        return pagingOptions == null ? null : new PageRequest(pagingOptions.pageIndex, pagingOptions.pageSize);
    }

    public void setPagingOptions(QueryPagingOptions pagingOptions) {
        this.pagingOptions = pagingOptions;
    }

    public EventQuery getQuery() {
        return query;
    }

    public void setQuery(EventQuery query) {
        this.query = query;
    }

}
