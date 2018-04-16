package eu.slipo.workbench.web.controller.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import eu.slipo.workbench.web.service.DefaultWebFileNamingStrategry;
import eu.slipo.workbench.web.service.IAuthenticationFacade;

public abstract class BaseController {

    @Autowired
    private IAuthenticationFacade authenticationFacade;

    @Autowired
    @Qualifier("defaultWebFileNamingStrategry")
    protected DefaultWebFileNamingStrategry fileNamingStrategy;

    protected int currentUserId() {
        return this.authenticationFacade.getCurrentUserId();
    }

}
