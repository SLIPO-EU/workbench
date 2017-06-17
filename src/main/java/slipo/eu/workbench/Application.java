package slipo.eu.workbench;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
@EnableAutoConfiguration
public class Application extends SpringBootServletInitializer {
   
    /**
     * Used when packaging as a WAR application
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) 
    {
        return builder.sources(Application.class);
    }
    
    /**
     * Used when packaging as a standalone JAR (the server is embedded)
     */
    public static void main(String[] args) 
    {
        SpringApplication.run(Application.class, args);
    }
}
