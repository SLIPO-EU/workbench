package eu.slipo.workbench.common.service;

import eu.slipo.workbench.common.domain.ProcessEntity;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStartException;
import eu.slipo.workbench.common.model.process.ProcessExecutionStopException;
import eu.slipo.workbench.common.model.process.ProcessNotFoundException;

public interface ProcessOperator
{
    /**
     * Start the execution of a process. The process is identified as the most recent 
     * {@link ProcessEntity} corresponding to the given id.
     * 
     * @see {@link ProcessOperator#start(long, long)}
     * 
     * @param id The process id
     * @return
     * @throws ProcessNotFoundException if no matching entity is found
     * @throws ProcessExecutionStartException if the execution failed to start
     */
    ProcessExecutionRecord start(long id) 
        throws ProcessNotFoundException, ProcessExecutionStartException;
    
    /**
     * Start the execution of a process revision. A revision is identified as the 
     * {@link ProcessRevisionEntity} with a given id and a given version. For such an entity,
     * only a single execution may be running at a given point of time.
     * 
     * @param id The process id
     * @param version The process (revision) version
     * @return
     * @throws ProcessNotFoundException if no matching entity is found
     * @throws ProcessExecutionStartException if the execution failed to start
     */
    ProcessExecutionRecord start(long id, long version) 
        throws ProcessNotFoundException, ProcessExecutionStartException;
    
    /**
     * Request from a process execution to stop. The process is identified as the most recent
     * {@link ProcessEntity} corresponding to the given id.
     * 
     * @see {@link ProcessOperator#stop(long, long)}
     * 
     * @param id The process id 
     * @throws ProcessNotFoundException if no matching entity is found
     * @throws ProcessExecutionStopException if we failed to signal a stop on the given process
     */
    void stop(long id) 
        throws ProcessNotFoundException, ProcessExecutionStopException;
    
    /**
     * Request from a process execution to stop. The process is identified as the
     * {@link ProcessRevisionEntity} with the given id and the given version.
     * 
     * <p>Note that, due to the inner mechanics of Spring-Batch, we can only request a stop
     * from a (running) process execution. A successful request does not offer a guarantee
     * that targeted execution did actually stop (or will ever stop). 
     * 
     * @see {@link ProcessOperator#start(long, long)}
     * 
     * @param id The process id
     * @param version The process (revision) version
     * @throws ProcessNotFoundException if no matching entity is found
     * @throws ProcessExecutionStopException if we failed to request a stop on the given process
     */
    void stop(long id, long version) 
        throws ProcessNotFoundException, ProcessExecutionStopException;
    
    /**
     * Poll the status of an execution (if any) for a process. The process is identified as 
     * the most recent {@link ProcessEntity} corresponding to the given id.
     * 
     * @param id The process ID 
     * @return <tt>null</tt> if no execution is found, else the latest execution record
     */
    ProcessExecutionRecord poll(long id);
    
    /**
     * Poll the status of an execution (if any) for a process. The process is identified as 
     * the {@link ProcessRevisionEntity} with the given id and the given version.
     * 
     * @param id The process ID 
     * @param version The process (revision) version
     * @return <tt>null</tt> if no execution is found, else the latest execution record
     */
    ProcessExecutionRecord poll(long id, long version);
}
