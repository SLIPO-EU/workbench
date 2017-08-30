package eu.slipo.workbench.common.model.jobs;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A DTO bean containing implementation-neutral information for a job instance.
 */
public class JobInstanceInfo
{
    /**
     * The job instance id.
     */
    private Long id;
    
    private String jobName;

    private JobInstanceInfo() {}
    
    public JobInstanceInfo(String jobName, long id)
    {
        this.id = id;
        this.jobName = jobName;
    }
    
    @JsonProperty("jobName")
    public String getJobName()
    {
        return jobName;
    }
    
    @JsonProperty("jobName")
    public void setJobName(String jobName)
    {
        this.jobName = jobName;
    }

    @JsonProperty("id")
    public Long getId()
    {
        return id;
    }
    
    @JsonProperty("id")
    public void setId(long id)
    {
        this.id = id;
    }
    
    
}
