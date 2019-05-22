package eu.slipo.workbench.web.controller.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.common.model.RestResponse;

@Secured({ "ROLE_API" })
@RestController("ApiContextController")
@RequestMapping(produces = "application/json")
public class ContextController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ContextController.class);

    /**
     * Validate application key
     *
     * @return An empty response if validation was successful or an error message
     */
    @GetMapping(value = "/api/v1/key/validate")
    public RestResponse<?> validate() {
        try {
            return RestResponse.success();
        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }
    }

    private RestResponse<?> exceptionToResponse(Exception ex) {
        logger.error(ex.getMessage(), ex);
        return exceptionToResponse(ex, Error.EnumLevel.ERROR);
    }

}
