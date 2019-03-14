package eu.slipo.workbench.web.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.PageRequest;

import eu.slipo.workbench.common.model.QueryResultPage;

/**
 * Generic query result
 *
 * @param <Result> the result class
 */
public class QueryResult<Result> {

    private QueryResultPagingOptions pagingOptions;

    private List<Result> items = new ArrayList<Result>();

    public QueryResult(QueryPagingOptions query, long count) {
        this.pagingOptions = new QueryResultPagingOptions(query.pageIndex, query.pageSize, count);
    }

    public QueryResult(int pageIndex, int pageSize, long count) {
        this.pagingOptions = new QueryResultPagingOptions(pageIndex, pageSize, count);
    }

    public QueryResult(QueryPagingOptions query, long count, List<Result> items) {
        this.pagingOptions = new QueryResultPagingOptions(query.pageIndex, query.pageSize, count);
        this.items.addAll(items);
    }

    public QueryResult(int pageIndex, int pageSize, long count, List<Result> items) {
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

    public static <R> QueryResult<R> create(QueryResultPage<R> p)
    {
        PageRequest pageReq = p.getPageRequest();
        return new QueryResult<R>(
            pageReq.getPageNumber(), pageReq.getPageSize(), (int) p.getCount(), p.getItems());
    }
}
