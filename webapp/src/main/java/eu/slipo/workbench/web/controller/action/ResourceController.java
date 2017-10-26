package eu.slipo.workbench.web.controller.action;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import eu.slipo.workbench.common.model.BasicErrorCode;
import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.web.model.EnumDataFormat;
import eu.slipo.workbench.web.model.EnumDataSource;
import eu.slipo.workbench.web.model.EnumResourceType;
import eu.slipo.workbench.web.model.FileInfo;
import eu.slipo.workbench.web.model.QueryPagingOptions;
import eu.slipo.workbench.web.model.QueryResult;
import eu.slipo.workbench.web.model.Resource;
import eu.slipo.workbench.web.model.ResourceErrorCode;
import eu.slipo.workbench.web.model.ResourceMetadata;
import eu.slipo.workbench.web.model.ResourceMetadataUpdate;
import eu.slipo.workbench.web.model.ResourceQuery;
import eu.slipo.workbench.web.model.ResourceRegistration;
import eu.slipo.workbench.web.model.process.ProcessConfiguration;
import eu.slipo.workbench.web.model.process.ProcessConfigurationBuilder;

/**
 * Actions for managing resources
 */
@RestController
public class ResourceController {

    private static final Logger logger = LoggerFactory.getLogger(ResourceController.class);

    /**
     * Folder where temporary files are saved. This folder must be accessible from the SLIPO service
     */
    @Value("${working.directory}")
    private String workingDirectory;

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
     * @return the process configuration
     */
    @RequestMapping(value = "/action/resource/register", method = RequestMethod.PUT, produces = "application/json")
    public RestResponse<?> registerResource(Authentication authentication, @RequestBody ResourceRegistration data) {
        // Convert registration data to a process configuration instance
        ProcessConfigurationBuilder builder = new ProcessConfigurationBuilder();

        ProcessConfiguration process =  builder
            .transientResource(1, data)
            .transform(data.getConfiguration(), 1, 2)
            .register(data.getMetadata(), 2)
            .build();

        // TODO: Submit request to service

        return RestResponse.result(process);
    }

    /**
     * Schedules the execution of a process for registering an uploaded resource
     *
     * @param authentication the authenticated principal
     * @param file uploaded resource file
     * @param data registration data
     * @return
     */
    @RequestMapping(value = "/action/resource/upload", method = RequestMethod.PUT, produces = "application/json")
    public RestResponse<?> uploadResource(
            Authentication authentication, @RequestPart("file") MultipartFile file,
            @RequestPart("data") ResourceRegistration data) {
        try {
            // Action supports only file uploading
            if (data.getSource() != EnumDataSource.UPLOAD) {
                return RestResponse.error(ResourceErrorCode.DATASOURCE_NOT_SUPPORTED, "Only data source of type 'UPLOAD' is supported.");
            }

            // Create a temporary file
            final String inputFile = createTemporaryFilename(file.getBytes());

            // Convert registration data to a process configuration instance
            ProcessConfigurationBuilder builder = new ProcessConfigurationBuilder();

            ProcessConfiguration process =  builder
                .fileResource(1, inputFile)
                .transform(data.getConfiguration(), 1, 2)
                .register(data.getMetadata(), 2)
                .build();

            //  TODO: Submit request to service

            return RestResponse.result(process);
        } catch(IOException ex) {
            logger.error(ex.getMessage(), ex);

            return RestResponse.error(BasicErrorCode.IO_ERROR, "IO exception has occured: " + ex.getMessage());
        }
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

    /**
     * Creates a new unique filename and stores the given array of bytes.
     *
     * @param data the content to write to the file
     * @return a unique filename.
     * @throws IOException in case of an I/O error
     */
    private String createTemporaryFilename(byte[] data) throws IOException {
        // Create working directory if not already exists
        FileUtils.forceMkdir(new File(workingDirectory));

        String filename = Paths.get(workingDirectory, UUID.randomUUID().toString()).toString();

        FileUtils.writeByteArrayToFile(new File(filename), data);

        return filename;
    }

    private Resource createResource(long id, int version) {
        return this.createResource(id, version, null);
    }

    private Resource createResource(long id, int version, ResourceMetadata metadata) {
        Resource resource = new Resource(id, version);

        resource.setCreatedOn(ZonedDateTime.now());
        resource.setUpdatedOn(resource.getCreatedOn());

        resource.setFile(new FileInfo(10, "file.xml", "storage/file.xml", ZonedDateTime.now()));

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
