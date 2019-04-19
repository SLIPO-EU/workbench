package eu.slipo.workbench.web.controller.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.slipo.workbench.common.model.BasicErrorCode;
import eu.slipo.workbench.common.model.ErrorCode;
import eu.slipo.workbench.common.model.RestResponse;

@Component
public class UploadHandlerExceptionResolver implements HandlerExceptionResolver {

    @Value("${spring.http.multipart.max-request-size}")
    private String maxRequestSize;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ModelAndView resolveException(
        HttpServletRequest request, HttpServletResponse response, Object object, Exception ex
    ) {
        this.createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response, ex);

        return new ModelAndView();
    }

    private void createErrorResponse(int status, HttpServletResponse response, Exception exception) {
        try {
            if (exception instanceof MultipartException) {
                ErrorCode code = BasicErrorCode.REQUEST_SIZE_REJECTED;
                String message = String.format("Request size exceeds max size (%s)", maxRequestSize);

                RestResponse<?> data = RestResponse.error(code, message);

                response.setStatus(status);
                response.getOutputStream().print(objectMapper.writeValueAsString(data));
            }
        } catch (Exception ex) {
            // Ignore
        }
    }

}
