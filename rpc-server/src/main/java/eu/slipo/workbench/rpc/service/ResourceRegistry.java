package eu.slipo.workbench.rpc.service;

import java.nio.file.Path;

import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.resource.ResourceMetadataCreate;
import eu.slipo.workbench.common.model.resource.ResourceRecord;

/**
 * A facade interface for registering file resources to the catalog
 */
public interface ResourceRegistry
{
    /**
     * Register file resources from the output of a process execution step.
     * 
     * @param executionId The id of the targeted process execution
     * @param stepKey The key of the target step inside the execution 
     * @param metadata The metadata that accompany this file resource
     * @return
     */
    ResourceRecord register(long executionId, int stepKey, ResourceMetadataCreate metadata);
    
    
}
