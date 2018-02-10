package eu.slipo.workbench.web.model;

import org.springframework.data.domain.PageRequest;

/**
 * Generic query with data pagination support
 * 
 * Fixme: replace with {@link PageRequest}
 */
public class Query {

    private QueryPagingOptions pagingOptions;

    public QueryPagingOptions getPagingOptions() {
        return pagingOptions;
    }

    public void setPagingOptions(QueryPagingOptions pagingOptions) {
        this.pagingOptions = pagingOptions;
    }

}
