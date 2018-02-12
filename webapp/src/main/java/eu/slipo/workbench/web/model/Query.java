package eu.slipo.workbench.web.model;

/**
 * Generic query with data pagination support
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
