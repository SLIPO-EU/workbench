package eu.slipo.workbench.rpc.service;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.slipo.workbench.common.domain.AccountEntity;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStartException;
import eu.slipo.workbench.common.model.process.ProcessExecutionStopException;
import eu.slipo.workbench.common.model.process.ProcessNotFoundException;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.common.repository.AccountRepository;
import eu.slipo.workbench.common.repository.ProcessRepository;
import eu.slipo.workbench.common.service.ProcessOperator;
import eu.slipo.workflows.Workflow;
import eu.slipo.workflows.WorkflowBuilderFactory;
import eu.slipo.workflows.WorkflowExecutionEventListener;
import eu.slipo.workflows.WorkflowExecutionSnapshot;
import eu.slipo.workflows.service.WorkflowScheduler;

@Service
public class DefaultProcessOperator implements ProcessOperator
{
    private static final Logger logger = LoggerFactory.getLogger(DefaultProcessOperator.class);
    
    @Autowired
    private ProcessRepository processRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private WorkflowScheduler workflowScheduler;
    
    @Autowired
    private WorkflowBuilderFactory workflowBuilderFactory;
    
    private class ExecutionListener implements WorkflowExecutionEventListener
    {
        private final long executionId;
        
        public ExecutionListener(long executionId)
        {
            this.executionId = executionId;
        }
        
        @Override
        public void onSuccess(WorkflowExecutionSnapshot workflowExecutionSnapshot)
        {
            // Todo Auto-generated method stub
        }

        @Override
        public void onFailure(WorkflowExecutionSnapshot workflowExecutionSnapshot)
        {
            // Todo Auto-generated method stub
        }

        @Override
        public void beforeNode(WorkflowExecutionSnapshot snapshot, String nodeName,
            JobExecution jobExecution)
        {
            // Todo Auto-generated method stub
        }

        @Override
        public void afterNode(WorkflowExecutionSnapshot snapshot, String nodeName,
            JobExecution jobExecution)
        {
            // Todo Auto-generated method stub
        }
    }
    
    /**
     * Build workflow from a process definition.
     * 
     * @param workflowId
     * @param definition The definition of a process revision
     * @return
     */
    private Workflow buildWorkflow(UUID workflowId, ProcessDefinition definition)
    {
        Workflow.Builder workflowBuilder = workflowBuilderFactory.get(workflowId);
        
        // Todo Build workflow
        
        return workflowBuilder.build();
    }
    
    /**
     * Start the execution of a workflow. The workflow is derived from definition of 
     * a process.
     * 
     * <p>Note: A workflow is 1-1 mapped to a process record (which in turn is a view of
     * a process revision entity). As a consequence, a process revision can only have a single 
     * active execution at a given time (because a workflow does so).
     * 
     * @param process
     * @return
     * @throws ProcessNotFoundException
     * @throws ProcessExecutionStartException
     */
    private ProcessExecutionRecord startExecution(ProcessRecord process, int uid)
        throws ProcessNotFoundException, ProcessExecutionStartException
    {
        Assert.state(process != null, "Expected a non-null process record");
        
        final long id = process.getId();
        final long version = process.getVersion();
        
        // Build a workflow from the definition of this process
        
        final UUID workflowId = UUID.nameUUIDFromBytes(
            ByteBuffer.wrap(new byte[16]) 
                .putLong(id).putLong(version)
                .array());
        Workflow workflow = buildWorkflow(workflowId, process.getDefinition());
        
        // Todo Create a process-execution entity and associate with captured events
        
        ProcessExecutionRecord processExecution = processRepository.createExecution(id, version, uid);
        
        // Todo return a DTO for process execution (ProcessExecutionRecord)
        
        throw new NotImplementedException("Todo");
    }
    
    private void stopExecution(ProcessRecord process)
    {
        // Todo Find and stop workflow associated with process of given (id,version)
        
        throw new NotImplementedException("Todo");
    }
    
    private ProcessExecutionRecord pollStatus(ProcessRecord process)
    {
        // Todo Poll the status of workflow associated with process of given (id,version)
        
        throw new NotImplementedException("Todo");
    }
    
    @Override
    public ProcessExecutionRecord start(long id, long version, int uid) 
        throws ProcessNotFoundException, ProcessExecutionStartException
    {
        Assert.isTrue(uid < 0 || accountRepository.exists(uid), "No user with given id");
        
        ProcessRecord process = processRepository.findOne(id, version);
        if (process == null)
            throw new ProcessNotFoundException(id, version);
        
        return startExecution(process, uid);
    }

    @Override
    public void stop(long id, long version) 
        throws ProcessNotFoundException, ProcessExecutionStopException
    {
        ProcessRecord r = processRepository.findOne(id, version);
        if (r == null)
            throw new ProcessNotFoundException(id, version);
        stopExecution(r);
    }

    @Override
    public ProcessExecutionRecord poll(long id, long version)
    {
        ProcessRecord r = processRepository.findOne(id, version);
        return r == null? null : pollStatus(r);
    }

}
