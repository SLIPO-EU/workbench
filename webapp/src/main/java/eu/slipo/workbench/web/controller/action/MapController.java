package eu.slipo.workbench.web.controller.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.model.process.CatalogResource;
import eu.slipo.workbench.common.model.process.EnumInputType;
import eu.slipo.workbench.common.model.process.ProcessErrorCode;
import eu.slipo.workbench.common.model.process.ProcessExecutionIdentifier;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.common.model.resource.ResourceRecord;
import eu.slipo.workbench.common.repository.ResourceRepository;
import eu.slipo.workbench.web.model.process.ProcessExecutionRecordView;
import eu.slipo.workbench.web.model.resource.MapDataResult;
import eu.slipo.workbench.web.model.resource.ResourceErrorCode;
import eu.slipo.workbench.web.service.ProcessService;

/**
 * Actions for accessing map data
 */
@RestController
@Secured({ "ROLE_USER", "ROLE_AUTHOR", "ROLE_ADMIN" })
@RequestMapping(produces = "application/json")
public class MapController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(MapController.class);

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ProcessService processService;

    /**
     * Get data for rendering a map for the process execution instance that created the
     * specified resource revision. The method returns data for all the resource
     * revisions.
     *
     * @param id the resource id
     * @param version the resource revision
     * @return the map metadata
     */
    @RequestMapping(value = "/action/map/resource/{id}/{version}", method = RequestMethod.GET)
    public RestResponse<?> getResource(@PathVariable long id, @PathVariable long version) {

        try {
            // Find resource
            final ResourceRecord resource = resourceRepository.findOne(id);
            if (resource == null) {
                return RestResponse.error(ResourceErrorCode.RESOURCE_NOT_FOUND, "Resource was not found");
            }

            this.checkResourceAccess(resource);

            // Check is requested revision exists
            final ResourceRecord revision = resource.getRevisions().stream()
                .filter(r -> r.getVersion() == version)
                .findFirst()
                .orElse(null);
            if (revision == null) {
                return RestResponse.error(ResourceErrorCode.RESOURCE_NOT_FOUND, "Resource was not found");
            }

            // Find process execution for the selected revision
            final ProcessExecutionIdentifier exId = revision.getExecution();
            final ProcessExecutionRecordView exData = (
                exId != null ?
                this.processService.getProcessExecution(exId.getProcessId(), exId.getProcessVersion(), exId.getExecutionId()) :
                null
            );

            if (exData == null) {
                return RestResponse.error(ProcessErrorCode.PROCESS_NOT_FOUND, "Process was not found");
            }

            this.loadProcessCatalogResources(exData.getProcess());

            return RestResponse.result(new MapDataResult(resource, exData.getProcess(), exData.getExecution(), version));
        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }
    }

    /**
     * Get data for rendering a map for the specified process execution instance.
     *
     * @param id the process id
     * @param version the process version
     * @param executionId the execution id
     * @return a list of {@link ProcessExecutionRecord}
     */
    @RequestMapping(value = "/action/map/process/{id}/{version}/execution/{executionId}", method = RequestMethod.GET)
    public RestResponse<?> getProcessExecution(
        @PathVariable long id, @PathVariable long version, @PathVariable long executionId
    ) {

        try {
            final ProcessExecutionRecordView view = this.processService.getProcessExecution(id, version, executionId);

            this.loadProcessCatalogResources(view.getProcess());

            return RestResponse.result(new MapDataResult(view.getProcess(), view.getExecution()));

        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }
    }

    private void loadProcessCatalogResources(ProcessRecord p) {
        p.getDefinition().resources().stream()
            .filter(r -> r.getInputType() == EnumInputType.CATALOG)
            .map(r -> (CatalogResource) r)
            .forEach(r -> {
                ResourceRecord resource = this.resourceRepository.findOne(r.getId(), r.getVersion());
                if (resource != null) {
                    r.refresh(resource);
                }
            });
    }

    private RestResponse<?> exceptionToResponse(Exception ex) {
        logger.error(ex.getMessage(), ex);
        return exceptionToResponse(ex, Error.EnumLevel.ERROR);
    }

}
