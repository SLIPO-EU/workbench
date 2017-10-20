package eu.slipo.workbench.web.controller.action;

import java.time.ZonedDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.web.model.EnumDataFormat;
import eu.slipo.workbench.web.model.EnumDataSource;
import eu.slipo.workbench.web.model.EnumResourceType;
import eu.slipo.workbench.web.model.File;
import eu.slipo.workbench.web.model.QueryPagingOptions;
import eu.slipo.workbench.web.model.QueryResult;
import eu.slipo.workbench.web.model.Resource;
import eu.slipo.workbench.web.model.ResourceMetadata;
import eu.slipo.workbench.web.model.ResourceMetadataUpdate;
import eu.slipo.workbench.web.model.ResourceQuery;
import eu.slipo.workbench.web.model.ResourceRegistration;

/**
 * Provides methods for querying and managing resources
 */
@RestController
public class ResourceController {

    /**
     * Search for resources
     *
     * @param authentication the authenticated principal
     * @param data the query to execute
     * @return a list of resources
     */
    @RequestMapping(value = "/action/resource/query", method = RequestMethod.POST, produces = "application/json")
    public RestResponse<QueryResult<Resource>> search(Authentication authentication, @RequestBody ResourceQuery data) {
        QueryPagingOptions pagingOptions = data.getPagingOptions();
        if (pagingOptions == null) {
            pagingOptions = new QueryPagingOptions();
            pagingOptions.pageIndex = 0;
            pagingOptions.pageSize = 10;

        } else if (pagingOptions.pageIndex < 0) {
            pagingOptions.pageIndex = 0;
        } else if (pagingOptions.pageSize <= 0) {
            pagingOptions.pageSize = 10;
        }

        int count = (pagingOptions.pageIndex + 1) * pagingOptions.pageSize + (int) (Math.random() * 100);

        QueryResult<Resource> result = new QueryResult<Resource>(pagingOptions, count);

        long id = pagingOptions.pageIndex + pagingOptions.pageSize + 1;

        for (int i = 0; i < pagingOptions.pageSize; i++) {
            id += i;
            result.addItem(this.createResource(id, 1));
        }

        return RestResponse.result(result);
    }

    /**
     * Schedules the execution of a process for registering a resource
     *
     * @param authentication the authenticated principal
     * @param data registration data
     * @return
     */
    @RequestMapping(value = "/action/resource/register", method = RequestMethod.PUT, produces = "application/json")
    public RestResponse<?> registerResource(Authentication authentication, @RequestBody ResourceRegistration data) {
        return RestResponse.result(null);
    }

    /**
     * Get the current version of the resource with the given id
     *
     * @param authentication the authenticated principal
     * @param id the resource id
     * @return the resource metadata
     */
    @RequestMapping(value = "/action/resource/{id}", method = RequestMethod.GET, produces = "application/json")
    public RestResponse<Resource> getResource(Authentication authentication, @PathVariable long id) {
        Resource resource = this.createResource(id, 1);

        resource.addVersion(this.createResource(id, 2));
        resource.addVersion(this.createResource(id, 3));
        resource.addVersion(this.createResource(id, 4));

        return RestResponse.<Resource>result(resource);
    }

    /**
     * Updates the metadata for a resource
     *
     * @param authentication the authenticated principal
     * @param metadata the resource metadata
     * @return the updated resource metadata
     */
    @RequestMapping(value = "/action/resource/{id}", method = RequestMethod.POST, produces = "application/json")
    public RestResponse<?> updateResource(Authentication authentication,  @PathVariable long id, @RequestBody ResourceMetadataUpdate data) {
        ResourceMetadata metadata = new ResourceMetadata(
            data.getName(),
            data.getDescription(),
            (int) (Math.random() * 100 + 100),
            EnumDataFormat.CSV,
            EnumDataFormat.N_TRIPLES
        );

        Resource resource = this.createResource(id, 1, metadata);

        resource.addVersion(this.createResource(id, 2));
        resource.addVersion(this.createResource(id, 3));
        resource.addVersion(this.createResource(id, 4));

        return RestResponse.<Resource>result(resource);
    }

    /**
     * Deletes a resource registration and all existing versions
     *
     * @param authentication the authenticated principal
     * @param id the resource id
     * @return
     */
    @RequestMapping(value = "/action/resource/{id}", method = RequestMethod.DELETE, produces = "application/json")
    public RestResponse<?> deleteResource(Authentication authentication, @PathVariable long id) {
        return RestResponse.result(null);
    }

    /**
     * Deletes the specific version of a resource
     *
     * @param authentication the authenticated principal
     * @param id the resource id
     * @param version the resource version
     * @return an instance {@link
     */
    @RequestMapping(value = "/action/resource/{id}/{version}", method = RequestMethod.DELETE, produces = "application/json")
    public RestResponse<?> deleteResource(Authentication authentication, @PathVariable long id, @PathVariable int version) {
        return RestResponse.result(null);
    }

    private Resource createResource(long id, int version) {
        return this.createResource(id, version, null);
    }

    private Resource createResource(long id, int version, ResourceMetadata metadata) {
        Resource resource = new Resource(id, version);

        resource.setCreatedOn(ZonedDateTime.now());
        resource.setUpdatedOn(resource.getCreatedOn());

        resource.setFile(new File(10, "storage/file.xml"));

        resource.setSource(EnumDataSource.UPLOAD);
        resource.setType(EnumResourceType.POI_DATA);

        if(metadata!=null) {
            resource.setMetadata(
                new ResourceMetadata(
                    metadata.getName(),
                    metadata.getDescription(),
                    (int) (Math.random() * 100 + 100),
                    metadata.getSourceFormat(),
                    EnumDataFormat.N_TRIPLES
                )
            );
        } else {
            resource.setMetadata(
                new ResourceMetadata(
                    String.format("Resource %d", id),
                    "Uploaded sample POI data",
                    (int) (Math.random() * 100 + 100),
                    EnumDataFormat.CSV,
                    EnumDataFormat.N_TRIPLES
                )
            );
        }


        return resource;
    }

}
