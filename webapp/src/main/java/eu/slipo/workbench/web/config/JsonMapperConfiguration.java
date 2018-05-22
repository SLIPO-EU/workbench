package eu.slipo.workbench.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Configuration
public class JsonMapperConfiguration {

    /**
     * Creates an instance of {@link ObjectMapper} and configures support for spatial
     * objects serialization
     *
     * @param builder a builder used to create {@link ObjectMapper} instances with a fluent API
     * @return a configured instance of {@link ObjectMapper}
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) 
    {
        ObjectMapper objectMapper = builder.build();
        objectMapper.registerModule(new JtsModule());
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return objectMapper;
    }
}
