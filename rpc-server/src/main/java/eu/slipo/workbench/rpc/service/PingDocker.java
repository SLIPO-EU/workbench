package eu.slipo.workbench.rpc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;

@Service
@ConditionalOnProperty("slipo.rpc-server.docker.ping")
public class PingDocker
{
    private static final Logger logger = LoggerFactory.getLogger(PingDocker.class);

    @Autowired
    DockerClient docker;

    @Scheduled(fixedRate = 5000L, initialDelay = 8000L)
    public void ping() throws DockerCertificateException
    {
        try {
            docker.ping();
        } catch (DockerException | InterruptedException ex) {
            logger.warn("The docker server did not respond: {}", ex.getMessage());
        }
    }
}
