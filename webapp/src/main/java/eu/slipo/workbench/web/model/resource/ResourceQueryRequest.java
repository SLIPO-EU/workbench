package eu.slipo.workbench.web.model.resource;

import org.springframework.data.domain.PageRequest;

import eu.slipo.workbench.common.model.resource.ResourceQuery;
import eu.slipo.workbench.web.model.QueryPagingOptions;

public class ResourceQueryRequest
{
    private QueryPagingOptions pagingOptions;
    
    private ResourceQuery query;

    public QueryPagingOptions getPagingOptions()
    {
        return pagingOptions;
    }

    public PageRequest getPageRequest()
    {
        return new PageRequest(pagingOptions.pageIndex, pagingOptions.pageSize);
    }
    
    public void setPagingOptions(QueryPagingOptions pagingOptions)
    {
        this.pagingOptions = pagingOptions;
    }

    public ResourceQuery getQuery()
    {
        return query;
    }

    public void setQuery(ResourceQuery query)
    {
        this.query = query;
    }
}
