package eu.slipo.workbench.web.model.admin;

import org.springframework.data.domain.PageRequest;

import eu.slipo.workbench.web.model.QueryPagingOptions;

public class ApplicationKeyQueryRequest {

    private QueryPagingOptions pagingOptions;

    private ApplicationKeyQuery query;

    public QueryPagingOptions getPagingOptions() {
        return pagingOptions;
    }

    public PageRequest getPageRequest() {
        return pagingOptions == null ? null : new PageRequest(pagingOptions.pageIndex, pagingOptions.pageSize);
    }

    public void setPagingOptions(QueryPagingOptions pagingOptions) {
        this.pagingOptions = pagingOptions;
    }

    public ApplicationKeyQuery getQuery() {
        return query;
    }

    public void setQuery(ApplicationKeyQuery query) {
        this.query = query;
    }
}
