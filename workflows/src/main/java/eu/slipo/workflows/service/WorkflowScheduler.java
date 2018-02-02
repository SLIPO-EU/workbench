package eu.slipo.workflows.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.batch.core.BatchStatus;

import eu.slipo.workflows.Workflow;
import eu.slipo.workflows.WorkflowExecutionCompletionListener;
import eu.slipo.workflows.WorkflowExecutionSnapshot;
import eu.slipo.workflows.WorkflowExecutionStopListener;
import eu.slipo.workflows.exception.WorkflowExecutionStartException;
import eu.slipo.workflows.exception.WorkflowExecutionStopException;

public interface WorkflowScheduler
{
    /**
     * A scheduler-level workflow execution status. Represents the overall status as 
     * aggregated from nodes that comprise the workflow.
     */
    enum ExecutionStatus
    {
        /** 
         * The workflow is started and running (at least one node is running). 
         * 
         * <p>Note: There may be failed nodes (i.e. at {@link BatchStatus#FAILED}), but the 
         * workflow is not yet characterized as failed (this will happen as soon as
         * all executions are finished).  
         */
        RUNNING, 
                
        /**
         * The workflow is stopped (nothing is running) due to a stop request.
         */
        STOPPED, 
        
        /**
         * The workflow is not running and at least one node reports as failed 
         * (i.e. {@link BatchStatus#FAILED}).
         */
        FAILED,
        
        /**
         * The workflow is not running and every node reports as complete (i.e at
         * {@link BatchStatus#COMPLETED})
         */
        COMPLETED;
    }
    
    /**
     * A scheduler-level snapshot of a workflow execution
     */
    interface ExecutionSnapshot
    {
        ExecutionStatus status();
        
        WorkflowExecutionSnapshot workflowExecutionSnapshot();
    }
    
    /**
     * Start (or restart) a workflow.
     * 
     * @param workflow The model (specification) of the workflow
     * @param callbacks An array 
     * @throws WorkflowExecutionStartException  
     */
    void start(Workflow workflow, WorkflowExecutionCompletionListener... callbacks) 
        throws WorkflowExecutionStartException;
    
    /**
     * Stop a workflow execution. 
     * 
     * @param workflowId The id of the workflow
     * @param callbacks
     * @throws WorkflowExecutionStopException
     */
    void stop(UUID workflowId, WorkflowExecutionStopListener... callbacks) 
        throws WorkflowExecutionStopException;
    
    /**
     * Take a snapshot of workflow execution
     * 
     * @param workflowId The id of the workflow
     */
    ExecutionSnapshot poll(UUID workflowId);
    
    /**
     * Get the scheduler-level status of the workflow execution
     * 
     * @param workflowId The id of the workflow
     */
    ExecutionStatus status(UUID workflowId);
    
    /**
     * Fetch information on a workflow execution.
     * 
     * <p>This is similar to {@link WorkflowScheduler#poll(UUID)}, but instead returns a 
     * DTO object which is more suitable (e.g is serializable) for exchange between other 
     * services or controllers (or remote clients).
     * 
     * @param workflowId The id of the workflow
     */
    WorkflowExecutionInfo info(UUID workflowId);
    
    /**
     * List all known workflow identifiers
     */
    Iterable<UUID> list();
}
