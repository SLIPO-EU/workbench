package eu.slipo.workbench.web.controller.action;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import eu.slipo.workbench.common.model.ApplicationException;
import eu.slipo.workbench.common.model.BasicErrorCode;
import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.common.model.QueryResultPage;
import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.process.EnumProcessTaskType;
import eu.slipo.workbench.common.model.process.InvalidProcessDefinitionException;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessDefinitionBuilder;
import eu.slipo.workbench.common.model.process.ProcessErrorCode;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStartException;
import eu.slipo.workbench.common.model.process.ProcessNotFoundException;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.common.model.resource.DataSource;
import eu.slipo.workbench.common.model.resource.FileSystemDataSource;
import eu.slipo.workbench.common.model.resource.ResourceMetadataCreate;
import eu.slipo.workbench.common.model.resource.ResourceMetadataUpdate;
import eu.slipo.workbench.common.model.resource.ResourceQuery;
import eu.slipo.workbench.common.model.resource.ResourceRecord;
import eu.slipo.workbench.common.model.resource.UploadDataSource;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;
import eu.slipo.workbench.common.repository.ProcessRepository;
import eu.slipo.workbench.common.repository.ResourceRepository;
import eu.slipo.workbench.web.model.QueryResult;
import eu.slipo.workbench.web.model.resource.RegistrationRequest;
import eu.slipo.workbench.web.model.resource.ResourceErrorCode;
import eu.slipo.workbench.web.model.resource.ResourceQueryRequest;
import eu.slipo.workbench.web.model.resource.ResourceRegistrationRequest;
import eu.slipo.workbench.web.model.resource.ResourceResult;
import eu.slipo.workbench.web.service.AuthenticationFacade;
import eu.slipo.workbench.web.service.IResourceValidationService;
import eu.slipo.workbench.web.service.ProcessService;

/**
 * Actions for managing resources
 */
@RestController
@Secured({ "ROLE_USER", "ROLE_ADMIN" })
@RequestMapping(produces = "application/json")
public class ResourceController {

    private static final Logger logger = LoggerFactory.getLogger(ResourceController.class);

    @Autowired
    @Qualifier("tempDataDirectory")
    private Path tempDir;

    @Autowired
    @Qualifier("catalogDataDirectory")
    private Path catalogDataDir;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private IResourceValidationService resourceValidationService;

    @Autowired
    private ProcessService processService;

    private int currentUserId() {
        return authenticationFacade.getCurrentUserId();
    }

    /**
     * Register a resource from a generic data source (i.e. {@link DataSource})
     *
     * @param source The data source
     * @param configuration The configuration needed by TripleGeo to transform into one of
     * the internally recognized data formats (see {@link EnumDataFormat}).
     * @param metadata The metadata that should accompany the catalog resource
     * @return
     *
     * @throws InvalidProcessDefinitionException if validation has failed
     * @throws ProcessNotFoundException if no matching revision entity is found
     * @throws ProcessExecutionStartException if the execution failed to start
     * @throws IOException if an I/O error has occurred
     */
    private ProcessRecord register(
        DataSource source, TriplegeoConfiguration configuration, ResourceMetadataCreate metadata
    ) throws InvalidProcessDefinitionException, ProcessNotFoundException, ProcessExecutionStartException, IOException {

        Assert.notNull(source, "A data source is required");
        Assert.notNull(configuration, "Expected configuration for Triplegeo transformation");
        Assert.notNull(metadata, "Expected metadata for resource registration");
        Assert.isTrue(!StringUtils.isEmpty(metadata.getName()), "A non-empty name is required");

        final int resourceKey = 1;
        final String procName = String.format("Resource registration: %s", metadata.getName());

        ProcessDefinition definition = ProcessDefinitionBuilder.create(procName)
            .transform("transform", stepBuilder -> stepBuilder
                .group(0)
                .source(source)
                .outputKey(resourceKey)
                .configuration(configuration))
            .register("register", stepBuilder -> stepBuilder
                .group(1)
                .resource(resourceKey)
                .metadata(metadata))
            .build();
        
        ProcessRecord record = processService.create(definition, EnumProcessTaskType.REGISTRATION);
        
        ProcessExecutionRecord executionRecord = processService.start(record.getId(), record.getVersion());
        logger.info(
            "A request for registration is submitted as execution #{}: metadata = {}", 
            executionRecord.getId(), metadata);
        
        return record;
    }

    /**
     * Search for resources
     *
     * @param data the query to execute
     * @return a list of resources
     */
    @RequestMapping(value = "/action/resource/query", method = RequestMethod.POST)
    public RestResponse<QueryResult<ResourceRecord>> find(@RequestBody ResourceQueryRequest request) {

        if (request == null || request.getQuery() == null) {
            return RestResponse.error(ResourceErrorCode.QUERY_IS_EMPTY, "The query is empty");
        }

        PageRequest pageRequest = request.getPageRequest();
        ResourceQuery query = request.getQuery();
        query.setCreatedBy(currentUserId());

        QueryResultPage<ResourceRecord> r = resourceRepository.find(query, pageRequest);
        return RestResponse.result(QueryResult.create(r));
    }

    /**
     * Schedules the execution of a process for registering a resource
     *
     * @param data registration data
     * @return the process configuration
     * @throws InvalidProcessDefinitionException
     */
    @RequestMapping(value = "/action/resource/register", method = RequestMethod.POST)
    public RestResponse<?> registerResource(@RequestBody ResourceRegistrationRequest request) {

        try {
            List<Error> errors = resourceValidationService.validate(request, currentUserId());
            if (!errors.isEmpty()) {
                return RestResponse.error(errors);
            }

            final ProcessRecord record = register(request.getDataSource(), request.getConfiguration(), request.getMetadata());

            return RestResponse.result(record);

            // TODO : Add single error handler
        } catch (ProcessNotFoundException e) {
            return RestResponse.error(ProcessErrorCode.NOT_FOUND, "Process was not found");
        } catch (ProcessExecutionStartException e) {
            return RestResponse.error(ProcessErrorCode.FAILED_TO_START, "Process execution has failed to start");
        } catch (IOException e) {
            return RestResponse.error(BasicErrorCode.IO_ERROR, "An unknown error has occurred");
        } catch (InvalidProcessDefinitionException ex) {
            return RestResponse.error(ex.getErrors());
        } catch (ApplicationException ex) {
            return RestResponse.error(ex.toError());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return RestResponse.error(BasicErrorCode.UNKNOWN, "An unknown error has occurred");
        }
    }

    /**
     * Schedules the execution of a process for registering an uploaded resource
     *
     * @param file uploaded resource file
     * @param data registration data
     * @throws InvalidProcessDefinitionException
     */
    @RequestMapping(value = "/action/resource/upload", method = RequestMethod.POST)
    public RestResponse<?> uploadResource(@RequestPart("file") MultipartFile file, @RequestPart("data") RegistrationRequest request) {

        try {
            final Path inputPath = createTemporaryFile(
                file.getBytes(), FilenameUtils.getExtension(file.getOriginalFilename()));

            List<Error> errors = resourceValidationService
                .validate(request, currentUserId(), tempDir.resolve(inputPath));
            if (!errors.isEmpty()) {
                FileUtils.deleteQuietly(inputPath.toFile());
                return RestResponse.error(errors);
            }

            final DataSource source = new FileSystemDataSource(inputPath);
            final ProcessRecord record = register(source, request.getConfiguration(), request.getMetadata());

            return RestResponse.result(record);

            // TODO : Add single error handler
        } catch (ProcessNotFoundException e) {
            return RestResponse.error(ProcessErrorCode.NOT_FOUND, "Process was not found");
        } catch (ProcessExecutionStartException e) {
            return RestResponse.error(ProcessErrorCode.FAILED_TO_START, "Process execution has failed to start");
        } catch (IOException e) {
            return RestResponse.error(BasicErrorCode.IO_ERROR, "An unknown error has occurred");
        } catch (InvalidProcessDefinitionException ex) {
            return RestResponse.error(ex.getErrors());
        } catch (ApplicationException ex) {
            return RestResponse.error(ex.toError());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return RestResponse.error(BasicErrorCode.UNKNOWN, "An unknown error has occurred");
        }
    }

    /**
     * Get the current version of the resource with the given id
     *
     * @param id the resource id
     * @return the resource metadata
     */
    @RequestMapping(value = "/action/resource/{id}", method = RequestMethod.GET)
    public RestResponse<ResourceResult> getResource(@PathVariable long id) {

        final ResourceRecord resource = resourceRepository.findOne(id);
        final ProcessExecutionRecord execution = this.getExecution(resource);

        return RestResponse.result(new ResourceResult(resource, execution));
    }

    /**
     * Get the current version of the resource with the given id
     *
     * @param id the resource id
     * @return the resource metadata
     */
    @RequestMapping(value = "/action/resource/{id}/{version}", method = RequestMethod.GET)
    public RestResponse<ResourceResult> getResource(@PathVariable long id, @PathVariable long version) 
    {
        final ResourceRecord resource = resourceRepository.findOne(id, version);
        final ProcessExecutionRecord execution = this.getExecution(resource);
        return RestResponse.result(new ResourceResult(resource, execution));
    }

    /**
     * Updates the metadata for a resource
     *
     * @param metadata the resource metadata
     * @return the updated resource metadata
     */
    @RequestMapping(value = "/action/resource/{id}", method = RequestMethod.POST)
    public RestResponse<?> updateResource(@PathVariable long id, @RequestBody ResourceMetadataUpdate data) 
    {
        return RestResponse.result(resourceRepository.findOne(id));
    }

    /**
     * Deletes a resource registration and all existing versions
     *
     * @param id the resource id
     * @return
     */
    @RequestMapping(value = "/action/resource/{id}", method = RequestMethod.DELETE)
    public RestResponse<?> deleteResource(@PathVariable long id) 
    {
        return RestResponse.result(null);
    }

    /**
     * Deletes the specific version of a resource
     *
     * @param id the resource id
     * @param version the resource version
     * @return an instance {@link
     */
    @RequestMapping(value = "/action/resource/{id}/{version}", method = RequestMethod.DELETE)
    public RestResponse<?> deleteResource(@PathVariable long id, @PathVariable int version) 
    {
        return RestResponse.result(null);
    }

    /**
     * Creates a new temporary file and stores the given array of bytes.
     *
     * @param data the content to write to the file
     * @throws IOException in case of an I/O error
     *
     * @return the file path under which data is written (the path will always be relative to
     *   server-side staging directory)
     */
    private Path createTemporaryFile(byte[] data, String extension) 
        throws IOException 
    {
        Path path = null;
        
        try {
            path = Files.createTempFile(tempDir, null, "." + (extension == null ? "dat" : extension));
            InputStream in = new ByteArrayInputStream(data);
            Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            logger.error("Failed to create temporary file", ex);
            throw ex;
        }
        
        return tempDir.relativize(path);
    }

    /**
     * Finds the process execution that created the given {@link @ResourceRecord} instance
     *
     * @param resource the resource instance
     * @return an instance of {@link ProcessExecutionRecord} or null if no record is found
     */
    private ProcessExecutionRecord getExecution(ResourceRecord resource) {

        final Long id = (resource != null ? resource.getProcessExecutionId() : null);
        return (id != null ? processRepository.findExecution(id) : null);
    }
}
