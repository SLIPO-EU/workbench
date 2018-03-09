package eu.slipo.workbench.common.service;

import java.io.IOException;

import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStartException;
import eu.slipo.workbench.common.model.process.ProcessExecutionStopException;
import eu.slipo.workbench.common.model.process.ProcessNotFoundException;

public interface ProcessOperator
{ 
    /**
     * Start the execution of a process revision. A revision will be identified as the 
     * {@link ProcessRevisionEntity} with the given id and version. For such an entity,
     * the application will enforce a single execution running at a given point of time.
     * 
     * @param id The process id
     * @param version The version of the process (revision)
     * @param submittedBy The id of the user that submitted this execution. A negative integer
     *   is interpreted as the entire application.
     * @param 
     * 
     * @throws ProcessNotFoundException if no matching revision entity is found
     * @throws ProcessExecutionStartException if the execution failed to start
     * @throws IOException 
     */
    ProcessExecutionRecord start(long id, long version, int submittedBy) 
        throws ProcessNotFoundException, ProcessExecutionStartException, IOException;
    
    /**
     * @see ProcessOperator#start(long, long, int)
     */
    default ProcessExecutionRecord start(long id, long version)
        throws ProcessNotFoundException, ProcessExecutionStartException, IOException 
    {
        return start(id, version, -1);
    }
    
    /**
     * Request from a process execution to stop. The execution will be identified as the
     * single execution associated with a {@link ProcessRevisionEntity} with the given id 
     * and version.
     * 
     * <p>Note that, due to the inner mechanics of Spring-Batch, we can only request a stop
     * from a (running) process execution. A successful request of this kind does not offer 
     * a guarantee that targeted execution did actually stop (or will ever stop). 
     * 
     * @see {@link ProcessOperator#start(long, long)}
     * 
     * @param id The process id
     * @param version The version of the process (revision)
     * 
     * @throws ProcessNotFoundException if no matching revision entity is found
     * @throws ProcessExecutionStopException if we failed to request a stop on the given process
     */
    void stop(long id, long version) 
        throws ProcessNotFoundException, ProcessExecutionStopException;
    
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
