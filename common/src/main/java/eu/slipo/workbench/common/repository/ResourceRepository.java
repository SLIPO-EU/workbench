package eu.slipo.workbench.common.repository;

import org.springframework.data.domain.PageRequest;

import com.fasterxml.jackson.databind.JsonNode;

import eu.slipo.workbench.common.model.QueryResultPage;
import eu.slipo.workbench.common.model.process.ProcessExecutionStepRecord;
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
     * Find a single resource identified by the pair of (resource-name, owner). If the
     * corresponding resource has more than 1 revisions, the latest is returned.
     *
     * @param resourceName The resource name (assigned by creator)
     * @param createdBy The id of the user that created (and owns) this resource
     * @return an instance of {@link ResourceRecord} if it exists, else <tt>null</tt>
     */
    ResourceRecord findOne(String resourceName, Integer createdBy);

    /**
     * Find a single resource identified by the process execution that created it.
     *
     * @param id The process execution id.
     * @return an instance of {@link ResourceRecord} if it exists, else <tt>null</tt>
     */
    ResourceRecord findOneByExecutionId(long id);

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
     * @return an resource record for the newly created entity
     */
    ResourceRecord createFromProcessExecution(long executionId, int stepKey, ResourceMetadataCreate metadata);

    /**
     * Update an existing resource entity creating a new revision.
     *
     * @param id The id of the targeted resource entity
     * @param record The DTO record that models our entity
     * @param updatedBy The id of the user that updated this resource
     * @return a resource record for the updated entity
     */
    ResourceRecord update(long id, ResourceRecord record, int updatedBy);

    /**
     * Link a resource entity to a given process execution.
     *
     * <p>This method does <em>not</em> affect versioning of resources in any way: simply links to
     * a given process execution. It expects to find a null link, and will refuse to update a non-null
     * link (different from the one given one).
     *
     * <p>The main reason this method is provided is that sometimes the information related to
     * process execution is not be available to the Batch job that created/updated the target resource
     * entity. In such a case, we can set this link in a post-processing callback (e.g. in a completion
     * listener).
     *
     * @param id The id of the resource entity
     * @param version The version of the resource entity
     * @param executionId The id of a process execution
     * @param stepKey The key of a specific step inside the execution; may be <tt>null</tt>.
     *   If <tt>null</tt>, it will be ignored; otherwise the corresponding step entity will also
     *   update its reference to the resource: the file identified by <tt>partKey</tt> will point
     *   to the target resource.
     * @param partKey The key of the output part of the step identified by <tt>stepKey</tt>. This
     *   may only be <tt>null</tt>, if <tt>stepKey</tt> is also null.
     * @return a resource record for the updated entity
     */
    void setProcessExecution(long id, long version, long executionId, Integer stepKey, String partKey);

    /**
     * Sets a custom style for rendering a map layer for the specific resource revision.
     *
     * @param id The id of the parent resource
     * @param version The version of the resource revision
     * @param style A JSON representation of the style
     */
    void setResourceRevisionStyle(long id, long version, JsonNode style);

    default void setProcessExecution(long id, long version, long executionId)
    {
        setProcessExecution(id, version, executionId, null, null);
    }
}
