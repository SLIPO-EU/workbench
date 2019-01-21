package eu.slipo.workbench.web.controller.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.web.model.process.ProcessExecutionRecordView;
import eu.slipo.workbench.web.model.provenance.ProvenanceRequest;
import eu.slipo.workbench.web.service.ProcessService;
import eu.slipo.workbench.web.service.ProvenanceService;

/**
 * Actions for accessing provenance data
 */
@RestController
@Secured({ "ROLE_USER", "ROLE_AUTHOR", "ROLE_ADMIN" })
@RequestMapping(produces = "application/json")
public class ProvenanceController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ProvenanceController.class);

    @Autowired
    private ProcessService processService;

    @Autowired
    private ProvenanceService featureProvenanceService;

    @RequestMapping(value = "/action/provenance/poi", method = RequestMethod.POST)
    public RestResponse<?> getFeatureTimeline(@RequestBody ProvenanceRequest request) {
        try {
            ProcessExecutionRecordView process = this.processService.getProcessExecution(
                request.getProcessId(),
                request.getProcessVersion(),
                request.getExecutionId()
            );

            ProvenanceService.Provenance provenance = this.featureProvenanceService.getPoiProvenance(
                process.getProcess().getDefinition(),
                process.getExecution(),
                request.getOutputKey(),
                request.getId(),
                request.getUri()
            );

            return RestResponse.result(provenance);

        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }
    }

    private RestResponse<?> exceptionToResponse(Exception ex) {
        logger.error(ex.getMessage(), ex);
        return exceptionToResponse(ex, Error.EnumLevel.ERROR);
    }

}
