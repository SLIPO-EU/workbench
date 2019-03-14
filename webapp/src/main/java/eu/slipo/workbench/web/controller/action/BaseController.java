package eu.slipo.workbench.web.controller.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import eu.slipo.workbench.common.model.ApplicationException;
import eu.slipo.workbench.common.model.BasicErrorCode;
import eu.slipo.workbench.common.model.EnumRole;
import eu.slipo.workbench.common.model.resource.ResourceRecord;

public abstract class BaseController extends eu.slipo.workbench.web.controller.BaseController {

    @Autowired
    protected MessageSource messageSource;

    protected void hasAnyRole(EnumRole... roles) {
        if (!this.authenticationFacade.hasAnyRole(roles)) {
            throw this.accessDenied();
        }
    }

    private ApplicationException accessDenied() {
        return ApplicationException
            .fromPattern(BasicErrorCode.AUTHORIZATION_FAILED)
            .withFormattedMessage(messageSource, currentUserLocale());
    }

    protected void checkResourceAccess(ResourceRecord record) {
        if ((!this.authenticationFacade.isAdmin()) && (!currentUserId().equals(record.getCreatedBy().getId()))) {
            throw this.accessDenied();
        }
    }

}
