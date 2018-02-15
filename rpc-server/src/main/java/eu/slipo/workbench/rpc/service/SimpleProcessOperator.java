package eu.slipo.workbench.rpc.service;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStartException;
import eu.slipo.workbench.common.model.process.ProcessExecutionStopException;
import eu.slipo.workbench.common.model.process.ProcessNotFoundException;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.common.repository.ProcessRepository;
import eu.slipo.workbench.common.service.ProcessOperator;
import eu.slipo.workflows.service.WorkflowScheduler;

@Service
public class SimpleProcessOperator implements ProcessOperator
{
    @Autowired
    private ProcessRepository processRepository;
    
    @Autowired
    private WorkflowScheduler workflowScheduler;
    
    private ProcessExecutionRecord startExecution(ProcessRecord process)
        throws ProcessNotFoundException, ProcessExecutionStartException
    {
        Assert.state(process != null, "Expected a non-null process record");
        
        // Todo Build a workflow from the definition of this process revision
        // A workflow instance should be 1-1 mapped to a process revision entity.
        // As a consequence, a process revision can only have a single active execution at 
        // a given time (because a workflow does so).  
        
        // Todo Create a process-execution entity and associate with captured events
        // Todo return a DTO for process execution (ProcessExecutionRecord)
        
        throw new NotImplementedException();
    }
    
    private void stopExecution(ProcessRecord process)
    {
        // Todo Find and stop workflow associated with process of given (id,version)
        
        throw new NotImplementedException();
    }
    
    private ProcessExecutionRecord pollStatus(ProcessRecord process)
    {
        // Todo Poll the status of workflow associated with process of given (id,version)
        
        throw new NotImplementedException();
    }
    
    @Override
    public ProcessExecutionRecord start(long id) 
        throws ProcessNotFoundException, ProcessExecutionStartException
    {
        ProcessRecord r = processRepository.findOne(id);
        if (r == null)
            throw new ProcessNotFoundException(id);
        return startExecution(r);
    }

    @Override
    public ProcessExecutionRecord start(long id, long version) 
        throws ProcessNotFoundException, ProcessExecutionStartException
    {
        ProcessRecord r = processRepository.findOne(id, version);
        if (r == null)
            throw new ProcessNotFoundException(id, version);
        return startExecution(r);
    }

    @Override
    public void stop(long id) 
        throws ProcessNotFoundException, ProcessExecutionStopException
    {
        ProcessRecord r = processRepository.findOne(id);
        if (r == null)
            throw new ProcessNotFoundException(id);
        stopExecution(r);
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
    public ProcessExecutionRecord poll(long id)
    {
        ProcessRecord r = processRepository.findOne(id);
        return r == null? null : pollStatus(r);
    }

    @Override
    public ProcessExecutionRecord poll(long id, long version)
    {
        ProcessRecord r = processRepository.findOne(id, version);
        return r == null? null : pollStatus(r);
    }

}
