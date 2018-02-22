package eu.slipo.workbench.common.repository;

import org.springframework.data.domain.PageRequest;

import eu.slipo.workbench.common.model.QueryResultPage;
import eu.slipo.workbench.common.model.resource.ResourceIdentifier;
import eu.slipo.workbench.common.model.resource.ResourceMetadataCreate;
import eu.slipo.workbench.common.model.resource.ResourceQuery;
import eu.slipo.workbench.common.model.resource.ResourceRecord;


public interface ResourceRepository 
{
    /**
     * Find resources using an instance of {@link ResourceQuery}
     *
     * @param query the query to execute
     * @param pageReq
     */
    QueryResultPage<ResourceRecord> find(ResourceQuery query, PageRequest pageReq);

    /**
     * Find the most recent resource with a given id
     *
     * @param id The resource id
     * @return an instance of {@link ResourceRecord} if resource exists, else <tt>null</tt>
     */
    ResourceRecord findOne(long id);

    /**
     * Find the resource with a given id and version
     * 
     * @param id The resource id
     * @param version The version of a specific resource revision
     * @return an instance of {@link ResourceRecord} if resource exists, else <tt>null</tt>
     */
    ResourceRecord findOne(long id, long version);
    
    default ResourceRecord findOne(ResourceIdentifier resourceIdentifier)
    {
        return findOne(resourceIdentifier.getId(), resourceIdentifier.getVersion());
    }
    
    /**
     * Find a single record by name
     *
     * Todo: Lookup by pair of (name,creator)
     *
     * @param name the resource unique name
     * @return an instance of {@link ResourceRecord} if the resource exists, else <tt>null</tt>
     */
    ResourceRecord findOne(String name);
    
    /**
     * Create a new resource entity.
     * 
     * @param record The DTO record that models our entity
     * @param createdBy The id of the user that created this resource
     * @return
     */
    ResourceRecord create(ResourceRecord record, int createdBy);
    
    /**
     * Create a new resource entity (i.e register a resource) from the output of a processing step.
     * 
     * <p>Apart from any transactional characteristics, this method is functionally equivalent to creating
     * a resource (via {@link ResourceRepository#create(ResourceRecord, int)}) and then updating the resource
     * link inside the processing step (via {@link ProcessRepository#updateExecutionStep(long, int, ProcessExecutionStepRecord)}).
     * 
     * @param executionId The id of the process execution
     * @param stepKey The key of the step inside the execution 
     * @param metadata The metadata that accompany this file resource
     * @return
     */
    ResourceRecord createFromProcessExecution(long executionId, int stepKey, ResourceMetadataCreate metadata);
    
    /**
     * Update an existing resource entity creating a new revision.
     * 
     * @param id The id of the targeted resource entity
     * @param record The DTO record that models our entity
     * @param updatedBy The id of the user that updated this resource
     * @return
     */
    ResourceRecord update(long id, ResourceRecord record, int updatedBy);
}
