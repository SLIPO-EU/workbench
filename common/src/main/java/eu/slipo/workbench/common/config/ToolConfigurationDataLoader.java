package eu.slipo.workbench.common.config;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.ImmutableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import eu.slipo.workbench.common.model.tool.DeerConfiguration;
import eu.slipo.workbench.common.model.tool.FagiConfiguration;
import eu.slipo.workbench.common.model.tool.LimesConfiguration;
import eu.slipo.workbench.common.model.tool.ReverseTriplegeoConfiguration;
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
    
    private Path resourcePath(Resource r)
    {
        Path path = null;
        if (r instanceof FileSystemResource) {
            path = Paths.get(((FileSystemResource) r).getPath());
        } else if (r instanceof ClassPathResource) {
            path = Paths.get(((ClassPathResource) r).getPath());
        } else {
            throw new IllegalStateException("Did not expect a resource of type [" + r.getClass() + "]");
        }
        return path;
    }
    
    @Bean("triplegeo.configurationProfiles")
    public Map<String, TriplegeoConfiguration> configurationProfilesForTriplegeo() 
        throws IOException, ConversionFailedException
    {
        final String rootPath = "common/vendor/triplegeo/config/profiles";
        final Map<String, TriplegeoConfiguration> profiles = new HashMap<>();
        
        for (Resource r: resourceResolver.getResources("classpath:" + rootPath + "/*/config.properties")) {
            Path path = resourcePath(r);
            String profileName = path.getParent().getFileName().toString();
            // Convert properties to target configuration
            TriplegeoConfiguration config = propertiesConverter.propertiesToValue(r, TriplegeoConfiguration.class);
            // Set {mapping, classification} spec on this configuration
            config.setMappingSpec("classpath:" + rootPath + "/" + profileName + "/mappings.yml" );
            config.setClassificationSpec("classpath:" + rootPath + "/" + profileName + "/classification.csv");
            // Register this configuration profile 
            profiles.put(profileName.toLowerCase(), (TriplegeoConfiguration) ImmutableBean.create(config));
        }
        
        return Collections.unmodifiableMap(profiles);
    }
    
    @Bean("reverseTriplegeo.configurationProfiles")
    public Map<String, ReverseTriplegeoConfiguration> configurationProfilesForReverseTriplegeo()
        throws IOException, ConversionFailedException
    {
        final String rootPath = "common/vendor/reverseTriplegeo/config/profiles";
        final Map<String, ReverseTriplegeoConfiguration> profiles = new HashMap<>();
        
        for (Resource r: resourceResolver.getResources("classpath:" + rootPath + "/*/config.properties")) {
            Path path = resourcePath(r);
            String profileName = path.getParent().getFileName().toString();
            // Convert properties to target configuration
            ReverseTriplegeoConfiguration config = propertiesConverter
                .propertiesToValue(r, ReverseTriplegeoConfiguration.class);
            // Set query file for this configuration
            config.setSparqlFile("classpath:" + rootPath + "/" + profileName + "/query.sparql");
            // Register this configuration profile 
            profiles.put(profileName.toLowerCase(), (ReverseTriplegeoConfiguration) ImmutableBean.create(config));
        }
        
        return Collections.unmodifiableMap(profiles);
    }
    
    @Bean("limes.configurationProfiles")
    public Map<String, LimesConfiguration> configurationProfilesForLimes() 
        throws IOException, ConversionFailedException
    {
        final String rootPath = "common/vendor/limes/config/profiles";
        final Map<String, LimesConfiguration> profiles = new HashMap<>();
        
        for (Resource r: resourceResolver.getResources("classpath:" + rootPath + "/*/config.properties")) {
            Path path = resourcePath(r);
            String profileName = path.getParent().getFileName().toString();
            // Convert properties to target configuration
            LimesConfiguration config = propertiesConverter.propertiesToValue(r, LimesConfiguration.class);
            // Register this configuration profile 
            profiles.put(profileName.toLowerCase(), (LimesConfiguration) ImmutableBean.create(config));
        }
        
        return Collections.unmodifiableMap(profiles);
    }
    
    @Bean("fagi.configurationProfiles")
    public Map<String, FagiConfiguration> configurationProfilesForFagi() 
        throws IOException, ConversionFailedException
    {
        final String rootPath = "common/vendor/fagi/config/profiles";
        final Map<String, FagiConfiguration> profiles = new HashMap<>();
        
        for (Resource r: resourceResolver.getResources("classpath:" + rootPath + "/*/config.properties")) {
            Path path = resourcePath(r);
            String profileName = path.getParent().getFileName().toString();
            // Convert properties to target configuration
            FagiConfiguration config = propertiesConverter.propertiesToValue(r, FagiConfiguration.class);
            // Set rules spec on this configuration
            config.setRulesSpec("classpath:" + rootPath + "/" + profileName + "/rules.xml");
            // Register this configuration profile 
            profiles.put(profileName.toLowerCase(), (FagiConfiguration) ImmutableBean.create(config));
        }
        
        return Collections.unmodifiableMap(profiles);
    }
    
    @Bean("deer.configurationProfiles")
    public Map<String, DeerConfiguration> cnfigurationProfilesForDeer() 
        throws IOException, ConversionFailedException
    {
        final String rootPath = "common/vendor/deer/config/profiles";
        final Map<String, DeerConfiguration> profiles = new HashMap<>();
        
        for (Resource r: resourceResolver.getResources("classpath:" + rootPath + "/*/config.properties")) {
            Path path = resourcePath(r);
            String profileName = path.getParent().getFileName().toString();
            // Convert properties to target configuration
            DeerConfiguration config = propertiesConverter.propertiesToValue(r, DeerConfiguration.class);
            // Set spec (execution graph description) on this configuration
            config.setSpec("classpath:" + rootPath + "/" + profileName + "/spec.ttl");
            // Register this configuration profile 
            profiles.put(profileName.toLowerCase(), (DeerConfiguration) ImmutableBean.create(config));
        }
        
        return Collections.unmodifiableMap(profiles);
    }
}
