package eu.slipo.workbench.web.controller.api;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.web.model.Headers;
import eu.slipo.workbench.web.security.ApplicationKeySessionRegistry;

@Secured({ "ROLE_API" })
@RestController("ApiContextController")
@RequestMapping(produces = "application/json")
public class ContextController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ContextController.class);

    @Autowired
    ApplicationKeySessionRegistry applicationKeySessionRegistry;

    /**
     * Validate application key
     *
     * @return An empty response if validation was successful or an error message
     */
    @GetMapping(value = "/api/v1/key/validate")
    public RestResponse<?> validate(HttpServletResponse response) {
        try {
            String token = this.applicationKeySessionRegistry.keyToSessionToken(this.applicationKey());

            response.setHeader(Headers.API_SESSION_TOKEN, token);

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
