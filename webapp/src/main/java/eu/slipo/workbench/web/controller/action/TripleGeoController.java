package eu.slipo.workbench.web.controller.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.triplegeo.ml.mappings.FieldMatcher;
import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.common.model.RestResponse;

/**
 * Provides helper actions for TripleGeo
 */
@RestController
@Secured({ "ROLE_USER", "ROLE_AUTHOR", "ROLE_ADMIN" })
@RequestMapping(produces = "application/json")
public class TripleGeoController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(TripleGeoController.class);

    @GetMapping(value = "/action/triplegeo/mappings")
    public RestResponse<?> getMappings() {

        try {
            FieldMatcher fieldMatcher = new FieldMatcher();

            // TODO: Implement
            throw new UnsupportedOperationException("Action not implemented");
        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }
    }

    private RestResponse<?> exceptionToResponse(Exception ex) {
        logger.error(ex.getMessage(), ex);
        return exceptionToResponse(ex, Error.EnumLevel.ERROR);
    }

}
