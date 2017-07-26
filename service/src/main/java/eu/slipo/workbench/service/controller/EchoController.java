package eu.slipo.workbench.service.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.model.TextMessage;

@RestController
public class EchoController
{
    private static Logger logger = LoggerFactory.getLogger(EchoController.class);
    
    @RequestMapping(
        value = "/api/echo", 
        method = RequestMethod.POST, 
        produces = "application/json",
        consumes = "application/json"
    )
    public RestResponse<TextMessage> scratch1(@RequestBody TextMessage message)
    {
        String text = message.text();
        String comment = "Echoed by " + EchoController.class.getName();
        logger.info("Echoing a message: {}", text);
        return RestResponse.result(new TextMessage(42, text, comment));
    }
}
