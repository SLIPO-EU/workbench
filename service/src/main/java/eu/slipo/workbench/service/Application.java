package eu.slipo.workbench.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(
    scanBasePackageClasses = {
        eu.slipo.workbench.common.config._Marker.class,
        eu.slipo.workbench.common.service._Marker.class,
        eu.slipo.workbench.service.config._Marker.class,
        eu.slipo.workbench.service.service._Marker.class,
        eu.slipo.workbench.service.controller._Marker.class,
    }
)
@EntityScan(
    basePackageClasses = {
        eu.slipo.workbench.common.domain._Marker.class,
        eu.slipo.workbench.service.domain._Marker.class,
    }
)
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
