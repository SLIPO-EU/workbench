package eu.slipo.workbench.web.controller.action;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;

import eu.slipo.workbench.common.model.ApplicationException;
import eu.slipo.workbench.common.model.BasicErrorCode;
import eu.slipo.workbench.common.model.EnumRole;
import eu.slipo.workbench.common.model.process.EnumProcessTaskType;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.resource.ResourceRecord;
import eu.slipo.workbench.common.service.DirectoryTraverse;
import eu.slipo.workbench.web.service.DefaultWebFileNamingStrategry;
import eu.slipo.workbench.web.service.IAuthenticationFacade;

public abstract class BaseController {

    @Autowired
    protected IAuthenticationFacade authenticationFacade;

    @Autowired
    protected MessageSource messageSource;

    @Autowired
    @Qualifier("defaultWebFileNamingStrategry")
    protected DefaultWebFileNamingStrategry fileNamingStrategy;

    @Autowired
    protected DirectoryTraverse directoryTraverse;

    protected Integer currentUserId() {
        return this.authenticationFacade.getCurrentUserId();
    }

    protected Locale currentUserLocale() {
        return authenticationFacade.getCurrentUserLocale();
    }

    protected boolean hasRole(EnumRole role) {
        return this.authenticationFacade.hasRole(role);
    }

    protected void hasAnyRole(EnumRole... roles) {
        if (!this.authenticationFacade.hasAnyRole(roles)) {
            throw this.accessDenied();
        }
    }

    protected boolean isAdmin() {
        return this.hasRole(EnumRole.ADMIN);
    }

    private ApplicationException accessDenied() {
        return ApplicationException.fromPattern(BasicErrorCode.AUTHORIZATION_FAILED).withFormattedMessage(messageSource,
                currentUserLocale());
    }

    protected void checkExecutionAccess(ProcessExecutionRecord record) {
        if ((!this.authenticationFacade.isAdmin()) && (!currentUserId().equals(record.getSubmittedBy().getId()))) {
            throw this.accessDenied();
        }
        if ((!this.authenticationFacade.isAdmin()) && (record.getTaskType() != EnumProcessTaskType.DATA_INTEGRATION)) {
            throw this.accessDenied();
        }
    }

    protected void checkResourceAccess(ResourceRecord record) {
        if ((!this.authenticationFacade.isAdmin()) && (!currentUserId().equals(record.getCreatedBy().getId()))) {
            throw this.accessDenied();
        }
    }

}
