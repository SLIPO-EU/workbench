package eu.slipo.workbench.web.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QueryResult<Result> {

    private QueryResultPagingOptions pagingOptions;

    private List<Result> items = new ArrayList<Result>();

    public QueryResult(QueryPagingOptions query, int count) {
        this.pagingOptions = new QueryResultPagingOptions(query.pageIndex, query.pageSize, count);
    }

    public QueryResult(int pageIndex, int pageSize, int count) {
        this.pagingOptions = new QueryResultPagingOptions(pageIndex, pageSize, count);
    }

    public QueryResult(QueryPagingOptions query, int count, List<Result> items) {
        this.pagingOptions = new QueryResultPagingOptions(query.pageIndex, query.pageSize, count);
        this.items.addAll(items);
    }

    public QueryResult(int pageIndex, int pageSize, int count, List<Result> items) {
        this.pagingOptions = new QueryResultPagingOptions(pageIndex, pageSize, count);
        this.items.addAll(items);
    }

    public QueryResultPagingOptions getPagingOptions() {
        return pagingOptions;
    }

    public List<Result> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void addItem(Result r) {
        this.items.add(r);
    }

}
