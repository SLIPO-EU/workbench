package eu.slipo.workbench.web.controller.action;

import org.springframework.beans.factory.annotation.Autowired;

import eu.slipo.workbench.web.service.IAuthenticationFacade;

public abstract class BaseController {

    @Autowired
    private IAuthenticationFacade authenticationFacade;

    protected int currentUserId() {
        return this.authenticationFacade.getCurrentUserId();
    }

}
