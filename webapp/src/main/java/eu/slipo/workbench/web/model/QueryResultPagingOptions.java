package eu.slipo.workbench.web.model;

/**
 * Query result pagination options
 */
public class QueryResultPagingOptions extends QueryPagingOptions {

    public long count;

    public QueryResultPagingOptions(int indexPage, int indexSize, long count) {
        this.pageIndex = indexPage;
        this.pageSize = indexSize;
        this.count = count;
    }

}
