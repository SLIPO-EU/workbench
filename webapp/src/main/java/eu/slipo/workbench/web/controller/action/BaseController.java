package eu.slipo.workbench.web.controller.action;

import java.io.IOException;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.remoting.RemoteConnectFailureException;

import eu.slipo.workbench.common.model.ApplicationException;
import eu.slipo.workbench.common.model.BasicErrorCode;
import eu.slipo.workbench.common.model.EnumRole;
import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.model.process.InvalidProcessDefinitionException;
import eu.slipo.workbench.common.model.process.ProcessErrorCode;
import eu.slipo.workbench.common.model.process.ProcessExecutionNotFoundException;
import eu.slipo.workbench.common.model.process.ProcessExecutionStartException;
import eu.slipo.workbench.common.model.process.ProcessExecutionStopException;
import eu.slipo.workbench.common.model.process.ProcessNotFoundException;
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

    protected String currentUserName() {
        return this.authenticationFacade.getCurrentUserName();
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
        return ApplicationException
            .fromPattern(BasicErrorCode.AUTHORIZATION_FAILED)
            .withFormattedMessage(messageSource, currentUserLocale());
    }

    protected void checkResourceAccess(ResourceRecord record) {
        if ((!this.authenticationFacade.isAdmin()) && (!currentUserId().equals(record.getCreatedBy().getId()))) {
            throw this.accessDenied();
        }
    }

    protected RestResponse<?> exceptionToResponse(Exception ex, Error.EnumLevel level) {
        if (ex instanceof IOException) {
            return RestResponse.error(BasicErrorCode.IO_ERROR, "An unknown error has occurred", level);
        }

        if (ex instanceof ProcessNotFoundException) {
            return RestResponse.error(ProcessErrorCode.PROCESS_NOT_FOUND, "Process was not found", level);
        }
        if (ex instanceof ProcessExecutionStartException) {
            return RestResponse.error(ProcessErrorCode.FAILED_TO_START, "Process execution has failed to start", level);
        }
        if (ex instanceof ProcessExecutionStopException) {
            return RestResponse.error(ProcessErrorCode.FAILED_TO_STOP, "Process execution has failed to stop", level);
        }
        if (ex instanceof ProcessExecutionNotFoundException) {
            return RestResponse.error(ProcessErrorCode.EXECUTION_NOT_FOUND, "Process execution was not found");
        }

        if (ex instanceof InvalidProcessDefinitionException) {
            InvalidProcessDefinitionException typedEx = (InvalidProcessDefinitionException) ex;
            return RestResponse.error(typedEx.getErrors());
        }
        if (ex instanceof ApplicationException) {
            ApplicationException typedEx = (ApplicationException) ex;
            return RestResponse.error(typedEx.toError());
        }

        if (ex instanceof RemoteConnectFailureException) {
            return RestResponse.error(ProcessErrorCode.RPC_SERVER_UNREACHABLE,
                "Process execution has failed to start. RPC server is unreachable", level);
        }

        if (ex instanceof UnsupportedOperationException) {
            return RestResponse.error(BasicErrorCode.NOT_IMPLEMENTED, "Action is not implemented", level);
        }

        return RestResponse.error(BasicErrorCode.UNKNOWN, "An unknown error has occurred", level);
    }

}
