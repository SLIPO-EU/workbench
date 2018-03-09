package eu.slipo.workbench.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.workbench.common.model.BasicErrorCode;
import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.web.controller.action.ResourceController;

@ControllerAdvice(annotations = { RestController.class })
public class RestControllerAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ResourceController.class);

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public @ResponseBody RestResponse<Void> handleException(HttpMessageNotReadableException ex) {

        logger.error("Cannot parse input:" + ex.getMessage());

        return RestResponse.error(BasicErrorCode.INPUT_NOT_READABLE, "Cannot parse input: " + ex.getMessage());
    }
}
