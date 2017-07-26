package eu.slipo.workbench.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig
{
    private static int TIMEOUT_MILLIS = 3000;
    
    @Bean
    RestTemplate restTemplate(@Autowired RestTemplateBuilder builder)
    {
        builder = builder
            .setConnectTimeout(TIMEOUT_MILLIS);
        
        // Add request interceptors (ClientHttpRequestInterceptor) 
        // or message converters, if needed
        
        return builder.build();
    }
}
