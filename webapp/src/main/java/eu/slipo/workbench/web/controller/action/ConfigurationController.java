package eu.slipo.workbench.web.controller.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.web.config.MapProperties;
import eu.slipo.workbench.web.model.Configuration;

@RestController
@Secured({ "ROLE_USER", "ROLE_ADMIN" })
@RequestMapping(produces = "application/json")
public class ConfigurationController {

    @Autowired
    MapProperties mapProperties;

    @RequestMapping(value = "/action/configuration", method = RequestMethod.GET)
    public RestResponse<Configuration> getConfiguration() {
        return RestResponse.result(this.createConfiguration());
    }

    private Configuration createConfiguration() {
        Configuration config = new Configuration();

        config.setOsm(this.mapProperties.getOsm());
        config.setBingMaps(this.mapProperties.getBingMaps());

        return config;
    }

}
