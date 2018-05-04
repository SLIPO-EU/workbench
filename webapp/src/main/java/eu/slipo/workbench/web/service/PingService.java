package eu.slipo.workbench.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import eu.slipo.workbench.common.service.EchoService;

/**
 * Ping our RPC backend to ensure we are in touch.
 */
@Service
@ConditionalOnProperty("slipo.rpc-server.ping")
public class PingService
{
    private static final Logger logger = LoggerFactory.getLogger(PingService.class);

    @Autowired
    EchoService echoService;

    @Scheduled(fixedRate = 5000L)
    public void ping()
    {
        try {
            echoService.echo("Hello World", "el");
        } catch (Exception ex) {
            logger.warn("Did not receive echo from RPC server: {}", ex.getMessage());
        }
    }
}
