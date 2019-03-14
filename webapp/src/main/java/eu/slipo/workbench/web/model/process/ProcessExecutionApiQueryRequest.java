package eu.slipo.workbench.web.model.process;

import org.springframework.data.domain.PageRequest;

import eu.slipo.workbench.common.model.process.ApiCallQuery;
import eu.slipo.workbench.web.model.QueryPagingOptions;

public class ProcessExecutionApiQueryRequest {
    private QueryPagingOptions pagingOptions;

    private ApiCallQuery query;

    public QueryPagingOptions getPagingOptions() {
        return pagingOptions;
    }

    public PageRequest getPageRequest() {
        return pagingOptions == null ? null : new PageRequest(pagingOptions.pageIndex, pagingOptions.pageSize);
    }

    public void setPagingOptions(QueryPagingOptions pagingOptions) {
        this.pagingOptions = pagingOptions;
    }

    public ApiCallQuery getQuery() {
        return query;
    }

    public void setQuery(ApiCallQuery query) {
        this.query = query;
    }
}
