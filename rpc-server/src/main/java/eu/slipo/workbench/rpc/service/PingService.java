package eu.slipo.workbench.rpc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;

@Service
public class PingService
{
    private static final Logger logger = LoggerFactory.getLogger(PingService.class);

    @Autowired
    DockerClient docker;

    @Value("${slipo.rpc-server.docker.ping:true}")
    Boolean pingDockerEnabled;


    @Scheduled(fixedRate = 5000L, initialDelay = 8000L)
    public void pingDocker()
    {
        if (pingDockerEnabled) {
            try {
                docker.ping();
            } catch (DockerException | InterruptedException ex) {
                logger.warn("The docker server did not respond: {}", ex.getMessage());
            }
        }
    }
}
