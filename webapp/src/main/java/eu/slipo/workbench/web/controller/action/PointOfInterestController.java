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
import eu.slipo.workbench.web.model.evolution.Evolution;
import eu.slipo.workbench.web.model.evolution.EvolutionRequest;
import eu.slipo.workbench.web.model.process.ProcessExecutionRecordView;
import eu.slipo.workbench.web.model.provenance.Provenance;
import eu.slipo.workbench.web.model.provenance.ProvenanceRequest;
import eu.slipo.workbench.web.service.EvolutionService;
import eu.slipo.workbench.web.service.ProcessService;
import eu.slipo.workbench.web.service.ProvenanceService;

/**
 * Actions for accessing provenance data
 */
@RestController
@Secured({ "ROLE_USER", "ROLE_AUTHOR", "ROLE_ADMIN" })
@RequestMapping(produces = "application/json")
public class PointOfInterestController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(PointOfInterestController.class);

    @Autowired
    private ProcessService processService;

    @Autowired
    private ProvenanceService provenanceService;

    @Autowired
    private EvolutionService evolutionService;

    @RequestMapping(value = "/action/provenance/poi", method = RequestMethod.POST)
    public RestResponse<?> getProvenance(@RequestBody ProvenanceRequest request) {
        try {
            ProcessExecutionRecordView process = this.processService.getProcessExecution(
                request.getProcessId(),
                request.getProcessVersion(),
                request.getExecutionId()
            );

            Provenance result = this.provenanceService.getPoiProvenance(
                process.getProcess().getDefinition(),
                process.getExecution(),
                request.getOutputKey(),
                request.getId(),
                request.getUri()
            );

            return RestResponse.result(result);

        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }
    }

    @RequestMapping(value = "/action/evolution/poi", method = RequestMethod.POST)
    public RestResponse<?> getEvolution(@RequestBody EvolutionRequest request) {
        try {
            Evolution result = this.evolutionService.getPoiEvolution(
                request.getProcessId(),
                request.getProcessVersion(),
                request.getExecutionId(),
                request.getId(),
                request.getUri()
            );

            return RestResponse.result(result);

        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }
    }

    private RestResponse<?> exceptionToResponse(Exception ex) {
        logger.error(ex.getMessage(), ex);
        return exceptionToResponse(ex, Error.EnumLevel.ERROR);
    }

}
