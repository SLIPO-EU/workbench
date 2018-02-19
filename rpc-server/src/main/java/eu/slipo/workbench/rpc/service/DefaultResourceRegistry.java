package eu.slipo.workbench.rpc.service;

import org.springframework.stereotype.Service;

import eu.slipo.workbench.common.model.resource.ResourceMetadataCreate;
import eu.slipo.workbench.common.model.resource.ResourceRecord;

@Service
public class DefaultResourceRegistry implements ResourceRegistry
{
    @Override
    public ResourceRecord register(long executionId, int stepKey, ResourceMetadataCreate metadata)
    {
        
        // Todo Resolve process-related entities and gather resource information
        
        // Todo Determine the id of the about to be registered resource
        // Does it correspond to the process that generated it?
        
        // Todo Create/Update a ResourceEntity
        // Todo Update resource link inside ProcessExecutionStepFileEntity
        
        return null;
    }

}
