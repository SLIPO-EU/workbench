package eu.slipo.workbench.rpc.controller;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.model.TextMessage;

@RestController
public class EchoController
{
    private static Logger logger = LoggerFactory.getLogger(EchoController.class);
    
    @Autowired
    MessageSource messageSource;
    
    private AtomicInteger serial = new AtomicInteger(0);
    
    @RequestMapping(
        value = "/api/echo", 
        method = RequestMethod.POST, 
        produces = "application/json", 
        consumes = "application/json")
    public RestResponse<TextMessage> echo(
        @RequestBody TextMessage message, 
        @RequestParam(name = "locale", defaultValue = "en") String lang)
    {
        String text = message.text();
        
        String comment = messageSource.getMessage(
            "EchoController.echoedBy", 
            new Object[] { this.getClass().getName() }, 
            "Echoed by {0}",
            Locale.forLanguageTag(lang));
        
        int id = serial.incrementAndGet();
        
        logger.info("Echoing message #{}: {}", id, text);
        return RestResponse.result(new TextMessage(id, text, comment));
    }
}
