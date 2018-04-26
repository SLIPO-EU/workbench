package eu.slipo.workbench.rpc.service;

import java.util.UUID;

import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workflows.Workflow;
import eu.slipo.workflows.util.digraph.TopologicalSort.CycleDetected;

public interface ProcessToWorkflowMapper
{
    /**
     * Build a workflow from a given process definition.
     *
     * @param id The id (parent id) of a process
     * @param version The version of a process revision
     * @param definition The process definition
     * @param createdBy The id of the user that created the process
     * @return a workflow
     *
     * @throws CycleDetected if the process definition has cyclic dependencies
     */
    Workflow buildWorkflow(long id, long version, ProcessDefinition definition, int createdBy)
        throws CycleDetected;

    /**
     * Compute the workflow identifier from the process identifier (i.e pair of (id, version)).
     * This computation must be implemented as a deterministic function.
     *
     * @param id The id (parent id) of a process
     * @param version The version of a process revision
     * @return a workflow identifier
     */
    UUID computeWorkflowId(long id, long version);
}
