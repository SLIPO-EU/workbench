package eu.slipo.workbench.web.controller.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;

import eu.slipo.workbench.common.model.ApplicationException;
import eu.slipo.workbench.common.model.BasicErrorCode;
import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.model.TextMessage;
import eu.slipo.workbench.web.service.RpcClientEchoService;

@RestController
@RequestMapping(produces = "application/json")
public class PingController
{
    private static Logger logger = LoggerFactory.getLogger(PingController.class);
    
    @Autowired
    private RpcClientEchoService echoService;
    
    /**
     * Ping our RPC server (echo a text message).
     * <p>This is an application-level ping to help us determine if our backend server 
     * is up and running. 
     * 
     * @param text A text message to send (and expect to be echoed)
     * @return the round-trip time needed (milliseconds) for the message to be echoed 
     */
    @GetMapping(value = "/action/ping-rpc-server")
    public RestResponse<Long> pingRpcServer(
        @RequestParam(name = "text", defaultValue = "Hello World") String text)
    {
        TextMessage echoedText = null;
        Error error = null;
        long started = -1L, elapsed = -1L;
        try {
            started = System.currentTimeMillis();
            echoedText = echoService.echo(text);
            elapsed = System.currentTimeMillis() - started;
        } catch (ApplicationException ex) {
            error = new Error(ex.getErrorCode(), ex.getMessage());
        } catch (ResourceAccessException ex) {
            error = new Error(BasicErrorCode.IO_ERROR, ex.getMessage());
        }
        
        Assert.state(echoedText == null || echoedText.text().equals(text), 
            "Expected an echo of our original message");
        
        return echoedText != null?
            RestResponse.result(elapsed) : RestResponse.error(error);    
    }
}
