package eu.slipo.workbench.rpc.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;

@Configuration
public class DockerClientConfiguration
{
    /** 
     * The docker server URL 
     */
    @Value("${slipo.rpc-server.docker.connection-url:unix:///var/run/docker.sock}")
    private String url;
    
    @Bean
    public DockerClient dockerClient() throws DockerCertificateException
    {
        return DefaultDockerClient.fromEnv()
            .uri(url)
            .connectTimeoutMillis(2000L)
            .build();
    }
    
}