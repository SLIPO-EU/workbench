package eu.slipo.workbench.command.config;

import java.io.IOException;
import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig
{
    private static final int TIMEOUT_MILLIS = 3000;
    
    @Bean
    RestTemplate restTemplate(@Autowired RestTemplateBuilder builder)
    {
        builder = builder
            .setReadTimeout(TIMEOUT_MILLIS)
            .setConnectTimeout(TIMEOUT_MILLIS);
        
        // Add request interceptors (ClientHttpRequestInterceptor) 
        // or message converters, if needed
        
        return builder.build();
    }
}