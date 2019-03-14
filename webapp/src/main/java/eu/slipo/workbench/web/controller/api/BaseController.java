package eu.slipo.workbench.web.controller.api;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.slipo.workbench.common.model.BasicErrorCode;
import eu.slipo.workbench.common.model.ErrorCode;
import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.model.security.ApplicationKeyException;
import eu.slipo.workbench.common.model.security.ApplicationKeyRecord;

public abstract class BaseController extends eu.slipo.workbench.web.controller.BaseController{

    @Autowired
    private ObjectMapper objectMapper;

    protected ApplicationKeyRecord applicationKey() {
        return this.authenticationFacade.getApplicationKey();
    }

    protected void createErrorResponse(int status, HttpServletResponse response, Exception exception) {
        ErrorCode code;
        String message;

        if (exception instanceof ApplicationKeyException) {
            code = ((ApplicationKeyException) exception).getCode();
            message = exception.getMessage();
        } else {
            code = BasicErrorCode.UNKNOWN;
            message = "An unknown error has occurred.";
        }

        this.createErrorResponse(status, response, code, message);
    }

    protected void createErrorResponse(int status, HttpServletResponse response, ErrorCode code, String message) {

        try {
            RestResponse<?> data = RestResponse.error(code, message);

            response.setStatus(status);
            response.getOutputStream().print(objectMapper.writeValueAsString(data));
        } catch (Exception ex) {
            // Ignore
        }
    }

}
