package eu.slipo.workbench.web.model.admin;

import org.springframework.data.domain.PageRequest;

import eu.slipo.workbench.web.model.QueryPagingOptions;

public class AccountQueryRequest {

    private QueryPagingOptions pagingOptions;

    private AccountQuery query;

    public QueryPagingOptions getPagingOptions() {
        return pagingOptions;
    }

    public PageRequest getPageRequest() {
        return pagingOptions == null ? null : new PageRequest(pagingOptions.pageIndex, pagingOptions.pageSize);
    }

    public void setPagingOptions(QueryPagingOptions pagingOptions) {
        this.pagingOptions = pagingOptions;
    }

    public AccountQuery getQuery() {
        return query;
    }

    public void setQuery(AccountQuery query) {
        this.query = query;
    }

}
