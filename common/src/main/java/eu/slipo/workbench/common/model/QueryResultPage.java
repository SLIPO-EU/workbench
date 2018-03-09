package eu.slipo.workbench.common.model;

import java.util.List;

import org.springframework.data.domain.PageRequest;

public class QueryResultPage <R>
{
    private final PageRequest pageRequest;
    
    private final long count;
    
    private final List<R> items;

    public QueryResultPage(List<R> items, PageRequest pageRequest, long count)
    {
        this.pageRequest = pageRequest;
        this.count = count;
        this.items = items;
    }

    public PageRequest getPageRequest()
    {
        return pageRequest;
    }

    public long getCount()
    {
        return count;
    }

    public List<R> getItems()
    {
        return items;
    }
}
