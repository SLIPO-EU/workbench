package eu.slipo.workflows.service;

import java.util.List;
import java.util.UUID;

import org.springframework.batch.core.BatchStatus;

/**
 * A DTO object providing information on a scheduled workflow execution.
 * 
 * <p>Note that a bean of this class is not simply an informational view from the
 * a corresponding business-level object (i.e {@link WorkflowExecutionSnapshot} or 
 * {@link WorkflowExecution}), as it also includes scheduler-level information (e.g status).
 */
public class WorkflowExecutionInfo
{
    public static class NodeExecutionInfo
    {
        private String name;
        
        private String jobName;
        
        private BatchStatus batchStatus;
        
        private Long executionId;

        public NodeExecutionInfo() {}
              
        public NodeExecutionInfo(
            String name, String jobName, BatchStatus batchStatus, Long executionId)
        {
            this.name = name;
            this.jobName = jobName;
            this.batchStatus = batchStatus;
            this.executionId = executionId;
        }

        public String getJobName()
        {
            return jobName;
        }

        public void setJobName(String jobName)
        {
            this.jobName = jobName;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public BatchStatus getBatchStatus()
        {
            return batchStatus;
        }

        public void setBatchStatus(BatchStatus batchStatus)
        {
            this.batchStatus = batchStatus;
        }

        public Long getExecutionId()
        {
            return executionId;
        }

        public void setExecutionId(Long executionId)
        {
            this.executionId = executionId;
        }
    }
    
    private UUID id;
    
    private WorkflowScheduler.ExecutionStatus status;
    
    private List<NodeExecutionInfo> details;
    
    public WorkflowExecutionInfo()
    {}
    
    public WorkflowExecutionInfo(
        UUID workflowId, WorkflowScheduler.ExecutionStatus status, List<NodeExecutionInfo> details)
    {
        this.id = workflowId;
        this.status = status;
        this.details = details;
    }
    
    public WorkflowExecutionInfo(UUID workflowId, WorkflowScheduler.ExecutionStatus status)
    {
        this(workflowId, status, null);
    }
    
    public UUID getId()
    {
        return id;
    }

    public void setId(UUID workflowId)
    {
        this.id = workflowId;
    }

    public WorkflowScheduler.ExecutionStatus getStatus()
    {
        return status;
    }

    public void setStatus(WorkflowScheduler.ExecutionStatus status)
    {
        this.status = status;
    }

    public List<NodeExecutionInfo> getDetails()
    {
        return details;
    }

    public void setDetails(List<NodeExecutionInfo> details)
    {
        this.details = details;
    }
    
}
