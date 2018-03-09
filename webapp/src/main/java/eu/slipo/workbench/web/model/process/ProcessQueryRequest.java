package eu.slipo.workbench.web.model.process;

import org.springframework.data.domain.PageRequest;

import eu.slipo.workbench.common.model.process.ProcessQuery;
import eu.slipo.workbench.web.model.QueryPagingOptions;

public class ProcessQueryRequest
{
    private QueryPagingOptions pagingOptions;
    
    private ProcessQuery query;

    public QueryPagingOptions getPagingOptions()
    {
        return pagingOptions;
    }
    
    public PageRequest getPageRequest()
    {
        return pagingOptions == null? 
            null : new PageRequest(pagingOptions.pageIndex, pagingOptions.pageSize);
    }

    public void setPagingOptions(QueryPagingOptions pagingOptions)
    {
        this.pagingOptions = pagingOptions;
    }

    public ProcessQuery getQuery()
    {
        return query;
    }

    public void setQuery(ProcessQuery query)
    {
        this.query = query;
    }
}
