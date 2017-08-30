package eu.slipo.workbench.common.model.jobs;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A DTO bean containing implementation-neutral information for a job execution.
 */
public class JobExecutionInfo
{
    private String statusText;
    
    /**
     * The job execution id.
     */
    private Long xid;
    
    /**
     * The job instance id.
     */
    private Long id;
    
    private Date started;
    
    private Date finished;

    public JobExecutionInfo() {}
    
    public JobExecutionInfo(long id, long executionId) 
    {
        this.id = id;
        this.xid = executionId;
    }
    
    @JsonProperty("status")
    public String getStatusText()
    {
        return statusText;
    }

    @JsonProperty("status")
    public void setStatusText(String statusText)
    {
        this.statusText = statusText;
    }

    @JsonProperty("xid")
    public Long getExecutionId()
    {
        return xid;
    }
    
    @JsonProperty("xid")
    public void setExecutionId(long xid)
    {
        this.xid = xid;
    }

    @JsonProperty("id")
    public Long getId()
    {
        return id;
    }
    
    public Long getInstanceId()
    {
        return id;
    }
    
    @JsonProperty("id")
    public void setId(long id)
    {
        this.id = id;
    }

    @JsonProperty("started")
    public Date getStarted()
    {
        return started;
    }
    
    @JsonProperty("started")
    public void setStarted(Date started)
    {
        this.started = started;
    }

    @JsonProperty("finished")
    public Date getFinished()
    {
        return finished;
    }
    
    @JsonProperty("finished")
    public void setFinished(Date finished)
    {
        this.finished = finished;
    }
}
