package eu.slipo.workbench.web.controller.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.common.model.FileSystemErrorCode;
import eu.slipo.workbench.common.model.QueryResultPage;
import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.model.resource.ResourceQuery;
import eu.slipo.workbench.common.model.resource.ResourceRecord;
import eu.slipo.workbench.common.repository.ResourceRepository;
import eu.slipo.workbench.common.service.CatalogUserFileNamingStrategy;
import eu.slipo.workbench.web.model.QueryResult;
import eu.slipo.workbench.web.model.api.resource.ResourceSimpleRecord;
import eu.slipo.workbench.web.model.resource.ResourceErrorCode;
import eu.slipo.workbench.web.model.resource.ResourceQueryRequest;

@Secured({ "ROLE_API" })
@RestController("ApiResourceController")
@RequestMapping(produces = "application/json")
public class ResourceController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ResourceController.class);

    @Autowired
    private ResourceRepository resourceRepository;

    /**
     * Queries catalog resources
     *
     * @param request A query for filtering resources
     * @return An instance of {@link QueryResult} with {@link ResourceSimpleRecord} items.
     */
    @PostMapping(value = "/api/v1/resource")
    public RestResponse<?> query(@RequestBody ResourceQueryRequest request) {
        try {
            if (request == null || request.getQuery() == null) {
                return RestResponse.error(ResourceErrorCode.QUERY_IS_EMPTY, "The query is empty");
            }

            PageRequest pageRequest = request.getPageRequest();
            ResourceQuery query = request.getQuery();

            query.setCreatedBy(currentUserId());

            QueryResultPage<ResourceRecord> result = resourceRepository.find(query, pageRequest);

            return RestResponse.result(new QueryResult<ResourceSimpleRecord>(
                request.getPagingOptions(),
                result.getCount(),
                result.getItems().stream().map(r -> new ResourceSimpleRecord(r)).collect(Collectors.toList())
            ));
        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }
    }

    /**
     * Downloads a file
     *
     * @param id The resource id
     * @param version The resource revision
     * @return the requested file
     */
    @GetMapping(value = "/api/v1/resource/{id}/{version}")
    public FileSystemResource download(
        @PathVariable long id, @PathVariable long version, HttpServletResponse response
    ) throws IOException {
        String relativePath = "";

        try {
            ResourceRecord resource = resourceRepository.findOne(id, version);
            if(resource == null) {
                createErrorResponse(
                    HttpServletResponse.SC_NOT_FOUND, response, ResourceErrorCode.RESOURCE_NOT_FOUND, "Resource was not found"
                );
                return null;
            }
            relativePath = resource.getFilePath();

            if (StringUtils.isEmpty(relativePath)) {
                createErrorResponse(
                    HttpServletResponse.SC_BAD_REQUEST, response, FileSystemErrorCode.PATH_IS_EMPTY, "A path to the file is required"
                );
                return null;
            }

            final Path absolutePath = fileNamingStrategy.resolveExecutionPath(CatalogUserFileNamingStrategy.SCHEME + "://" + relativePath);
            final File file = absolutePath.toFile();

            if (!file.exists()) {
                createErrorResponse(
                    HttpServletResponse.SC_NOT_FOUND, response, FileSystemErrorCode.PATH_NOT_FOUND, "File does not exist"
                );
            } else if (file.isDirectory()) {
                createErrorResponse(
                    HttpServletResponse.SC_NOT_FOUND, response, FileSystemErrorCode.PATH_NOT_FOUND, "File path is a directory"
                );
            } else {
                logger.info("User {} ({}) has downloaded file {}",
                    this.currentUserName(),
                    this.currentUserId(),
                    absolutePath.toString());

                response.setHeader("Content-Disposition", String.format("attachment; filename=%s", file.getName()));
                return new FileSystemResource(file);
            }
        } catch (Exception ex) {
            logger.warn("Failed to download file [{}] for user {} ({})",
                relativePath,
                this.currentUserName(),
                this.currentUserId());

            createErrorResponse(
                HttpServletResponse.SC_GONE, response, FileSystemErrorCode.PATH_NOT_FOUND, "File has been removed"
            );
        }

        return null;
    }

    private RestResponse<?> exceptionToResponse(Exception ex) {
        logger.error(ex.getMessage(), ex);
        return exceptionToResponse(ex, Error.EnumLevel.ERROR);
    }

}