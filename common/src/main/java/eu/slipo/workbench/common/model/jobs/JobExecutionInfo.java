package eu.slipo.workbench.common.model.jobs;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A DTO bean containing implementation-neutral information for a job execution.
 */
public class JobExecutionInfo
{
    /**
     * A textual representation of the status (i.e batch-status) of a job.
     */
    private String status;
    
    /**
     * A textual representation of the exit-status of a job.
     */
    private String exitStatus;
    
    /**
     * A detailed description that may accompany the exit-status of a job
     */
    private String exitDescription;
    
    /**
     * The job execution id.
     */
    private Long executionId;
    
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
        this.executionId = executionId;
    }
    
    @JsonProperty("status")
    public String getStatus()
    {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String statusText)
    {
        this.status = statusText;
    }

    @JsonProperty("exitStatus")
    public String getExitStatus()
    {
        return exitStatus;
    }
    
    @JsonProperty("exitStatus")
    public void setExitStatus(String statusText)
    {
        this.exitStatus = statusText;
    }

    @JsonProperty("exitDescription")
    public String getExitDescription()
    {
        return exitDescription;
    }

    @JsonProperty("exitDescription")
    public void setExitDescription(String description)
    {
        this.exitDescription = description;
    }

    @JsonProperty("executionId")
    public Long getExecutionId()
    {
        return executionId;
    }
    
    @JsonProperty("executionId")
    public void setExecutionId(long xid)
    {
        this.executionId = xid;
    }

    @JsonProperty("id")
    public Long getId()
    {
        return id;
    }
    
    @JsonProperty("instanceId")
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
