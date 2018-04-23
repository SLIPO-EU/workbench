package eu.slipo.workbench.common.config;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.ImmutableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;
import eu.slipo.workbench.common.service.util.PropertiesConverterService;
import eu.slipo.workbench.common.service.util.PropertiesConverterService.ConversionFailedException;

/**
 * Load configuration data (profiles/defaults) for supported tools
 */
@Configuration
public class ToolConfigurationDataLoader
{
    @Autowired 
    private ResourcePatternResolver resourceResolver;
    
    @Autowired
    private PropertiesConverterService propertiesConverter;
    
    @Bean("triplegeo.configurationProfiles")
    public Map<String, TriplegeoConfiguration> triplegeoConfigurationProfiles() 
        throws IOException, ConversionFailedException
    {
        final String rootPath = "common/vendor/triplegeo/config/profiles";
        
        final Map<String, TriplegeoConfiguration> profiles = new HashMap<>();
        
        for (Resource r: resourceResolver.getResources("classpath:" + rootPath + "/*/options.conf")) {
            Path path = null;
            if (r instanceof FileSystemResource) {
                path = Paths.get(((FileSystemResource) r).getPath());
            } else if (r instanceof ClassPathResource) {
                path = Paths.get(((ClassPathResource) r).getPath());
            } else {
                throw new IllegalStateException("Did not expect a resource of type [" + r.getClass() + "]");
            }
            
            // The profile name is the name of matched directory
            Path parent = path.getParent();
            String name = parent.getFileName().toString();
            
            // Convert properties to target configuration
            TriplegeoConfiguration conf = 
                propertiesConverter.propertiesToValue(r, TriplegeoConfiguration.class);
            
            // Set mapping-spec, classification-spec on this configuration
            conf.setMappingSpec(
                "classpath:" + rootPath + "/" + name + "/mappings.yml" );
            conf.setClassificationSpec(
                "classpath:" + rootPath + "/" + name + "/classification.csv");
            
            // Register this configuration profile 
            profiles.put(
                name.toLowerCase(), (TriplegeoConfiguration) ImmutableBean.create(conf));
        }
        
        return Collections.unmodifiableMap(profiles);
    }
    
}
