package eu.slipo.workbench.web.model;

public class QueryResultPagingOptions extends QueryPagingOptions {

    public int count;

    public QueryResultPagingOptions(int indexPage, int indexSize, int count) {
        this.pageIndex = indexPage;
        this.pageSize = indexSize;
        this.count = count;
    }

}
