package eu.slipo.workbench.web.model.process;

import org.springframework.data.domain.PageRequest;

import eu.slipo.workbench.common.model.process.ProcessExecutionQuery;
import eu.slipo.workbench.web.model.QueryPagingOptions;

public class ProcessExecutionQueryRequest
{
    private QueryPagingOptions pagingOptions;
    
    private ProcessExecutionQuery query;

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

    public ProcessExecutionQuery getQuery()
    {
        return query;
    }

    public void setQuery(ProcessExecutionQuery query)
    {
        this.query = query;
    }
}
