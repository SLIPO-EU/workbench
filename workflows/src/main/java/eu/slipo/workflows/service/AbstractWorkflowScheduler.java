package eu.slipo.workflows.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import eu.slipo.workflows.Workflow;
import eu.slipo.workflows.WorkflowExecutionSnapshot;

public abstract class AbstractWorkflowScheduler implements WorkflowScheduler
{
    @Override
    public WorkflowExecutionInfo info(UUID workflowId)
    {
        Validate.notNull(workflowId, "The workflow ID is required");
        
        ExecutionSnapshot executionSnapshot = poll(workflowId);
        if (executionSnapshot == null)
            return null;
        
        final WorkflowExecutionSnapshot workflowExecutionSnapshot = 
            executionSnapshot.workflowExecutionSnapshot();
        
        final Workflow workflow = workflowExecutionSnapshot.workflow();
        Validate.validState(workflowId.equals(workflow.id()));
        
        final Set<String> nodeNames = workflowExecutionSnapshot.nodeNames();
        final List<WorkflowExecutionInfo.NodeExecutionInfo> details = nodeNames.stream()
            .map(name -> workflowExecutionSnapshot.node(name))
            .map(e -> {
                WorkflowExecutionInfo.NodeExecutionInfo y =
                    new WorkflowExecutionInfo.NodeExecutionInfo();
                y.setBatchStatus(e.status());
                y.setExecutionId(e.executionId());
                y.setJobName(e.jobName());
                y.setName(e.nodeName());
                return y;
            })
            .collect(Collectors.toList());
        
        return new WorkflowExecutionInfo(workflowId, executionSnapshot.status(), details);
    }
}
