package eu.slipo.workbench.web.controller.action;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import eu.slipo.workbench.web.model.QueryResult;
import eu.slipo.workbench.web.model.process.ProcessDefinitionUpdate;
import eu.slipo.workbench.web.model.process.ProcessDefinitionUpdateBuilder;
import eu.slipo.workbench.web.model.resource.EnumDataSourceType;
import eu.slipo.workbench.web.model.resource.RegistrationRequest;
import eu.slipo.workbench.web.model.resource.ResourceErrorCode;
import eu.slipo.workbench.web.model.resource.ResourceMetadataUpdate;
import eu.slipo.workbench.web.model.resource.ResourceQuery;
import eu.slipo.workbench.web.model.resource.ResourceRecord;
import eu.slipo.workbench.web.model.resource.ResourceRegistrationRequest;
import eu.slipo.workbench.web.model.resource.UploadDataSource;
import eu.slipo.workbench.web.repository.IResourceRepository;

/**
 * Actions for managing resources
 */
@RestController
public class ResourceController {

    private static final Logger logger = LoggerFactory.getLogger(ResourceController.class);

    @Autowired
    private IResourceRepository resourceRepository;

    /**
     * Folder where temporary files are saved. This folder must be accessible from the
     * SLIPO service
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
    public RestResponse<QueryResult<ResourceRecord>> find(Authentication authentication, @RequestBody ResourceQuery query) {

        if (query == null) {
            RestResponse.error(ResourceErrorCode.QUERY_IS_EMPTY, "The query is empty");
        }

        QueryResult<ResourceRecord> result = this.resourceRepository.find(query);

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
    public RestResponse<?> registerResource(Authentication authentication, @RequestBody ResourceRegistrationRequest request) {
        // Action does not support file uploading
        if (request.getDataSource().getType() == EnumDataSourceType.UPLOAD) {
            return RestResponse.error(ResourceErrorCode.DATASOURCE_NOT_SUPPORTED,
                    "Data source of type 'UPLOAD' is not supported.");
        }

        // Convert registration data to a process configuration instance
        ProcessDefinitionUpdate process = ProcessDefinitionUpdateBuilder.create()
                .transform(0, "Transform", request.getDataSource(), request.getSettings(), 1)
                .register(1, "Register", request.getMetadata(), 1)
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
    public RestResponse<?> uploadResource(Authentication authentication, @RequestPart("file") MultipartFile file,
            @RequestPart("data") RegistrationRequest request) {
        try {
            // Create a temporary file
            final String inputFile = createTemporaryFilename(file.getBytes(),
                    FilenameUtils.getExtension(file.getOriginalFilename()));

            // Convert registration data to a process configuration instance
            ProcessDefinitionUpdate process = ProcessDefinitionUpdateBuilder.create()
                    .transform(0, "Transform", new UploadDataSource(inputFile), request.getSettings(), 1)
                    .register(1, "Register", request.getMetadata(), 1).build();

            // TODO: Submit request to service

            return RestResponse.result(process);
        } catch (IOException ex) {
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
    public RestResponse<ResourceRecord> getResource(Authentication authentication, @PathVariable long id) {
        return RestResponse.<ResourceRecord>result(this.resourceRepository.findOne(id));
    }

    /**
     * Updates the metadata for a resource
     *
     * @param authentication the authenticated principal
     * @param metadata the resource metadata
     * @return the updated resource metadata
     */
    @RequestMapping(value = "/action/resource/{id}", method = RequestMethod.POST, produces = "application/json")
    public RestResponse<?> updateResource(Authentication authentication, @PathVariable long id, @RequestBody ResourceMetadataUpdate data) {

        return RestResponse.<ResourceRecord>result(this.resourceRepository.findOne(id));
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
    public RestResponse<?> deleteResource(Authentication authentication, @PathVariable long id,
            @PathVariable int version) {
        return RestResponse.result(null);
    }

    /**
     * Creates a new unique filename and stores the given array of bytes.
     *
     * @param data the content to write to the file
     * @return a unique filename.
     * @throws IOException in case of an I/O error
     */
    private String createTemporaryFilename(byte[] data, String extension) throws IOException {

        // Create working directory if not already exists
        FileUtils.forceMkdir(new File(workingDirectory));

        String filename = Paths.get(workingDirectory, UUID.randomUUID().toString()).toString();
        filename += (extension == null ? "" : "." + extension);

        FileUtils.writeByteArrayToFile(new File(filename), data);

        return filename;
    }

}
