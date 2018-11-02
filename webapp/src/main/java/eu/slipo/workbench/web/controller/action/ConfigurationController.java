package eu.slipo.workbench.web.controller.action;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.tool.DeerConfiguration;
import eu.slipo.workbench.common.model.tool.FagiConfiguration;
import eu.slipo.workbench.common.model.tool.LimesConfiguration;
import eu.slipo.workbench.common.model.tool.ToolConfiguration;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;
import eu.slipo.workbench.common.service.util.PropertiesConverterService;
import eu.slipo.workbench.web.config.MapConfiguration;
import eu.slipo.workbench.web.config.ToolkitConfiguration;
import eu.slipo.workbench.web.model.configuration.ClientConfiguration;

@RestController
@Secured({ "ROLE_USER", "ROLE_AUTHOR", "ROLE_ADMIN" })
@RequestMapping(produces = "application/json")
public class ConfigurationController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationController.class);

    final String vendorDataPath = "common/vendor";

    @Autowired
    private ResourcePatternResolver resourceResolver;

    @Autowired
    private PropertiesConverterService propertiesConverter;

    @Autowired
    private MapConfiguration mapConfiguration;

    @Autowired
    private ToolkitConfiguration toolkitConfiguration;

    @RequestMapping(value = "/action/configuration", method = RequestMethod.GET)
    public RestResponse<ClientConfiguration> getConfiguration() {
        return RestResponse.result(this.createConfiguration());
    }

    private ClientConfiguration createConfiguration() {
        ClientConfiguration config = new ClientConfiguration();

        config.setProfiles(this.getToolProfiles());
        config.setOsm(this.mapConfiguration.getOsm());
        config.setBingMaps(this.mapConfiguration.getBingMaps());
        config.setTripleGeo(toolkitConfiguration.getTriplegeo());
        config.setReverseTripleGeo(toolkitConfiguration.getReverseTriplegeo());
        config.setLimes(toolkitConfiguration.getLimes());
        config.setFagi(toolkitConfiguration.getFagi());
        config.setDeer(toolkitConfiguration.getDeer());
        config.setMapDefaults(this.mapConfiguration.getDefaults());

        return config;
    }

    private Map<EnumTool, Map<String, ToolConfiguration<?>>> getToolProfiles() {
        Map<EnumTool, Map<String, ToolConfiguration<?>>>  result = new HashMap<EnumTool, Map<String, ToolConfiguration<?>>>();
        try {
            String re = ".*vendor\\/(.*)\\/.*config\\/profiles\\/(.*)\\/(config\\.properties)";
            Pattern pattern = Pattern.compile(re);

            Stream.of(resourceResolver.getResources("classpath:" + vendorDataPath + "/**"))
            .map(r -> {
                Pair<Path, Resource> p = null;
                if (r instanceof FileSystemResource) {
                    p = Pair.<Path, Resource>of(Paths.get(((FileSystemResource) r).getPath()), r);
                } else if (r instanceof ClassPathResource) {
                    p = Pair.<Path, Resource>of(Paths.get(((ClassPathResource) r).getPath()), r);
                } else {
                    logger.warn("Did not expect a resource of type [" + r.getClass() + "]");
                }
                return p;
            })
            .forEach(pair -> {
                Matcher m = pattern.matcher(pair.getLeft().toString());
                if (m.matches()) {
                    EnumTool tool = EnumTool.fromName(m.group(1).toUpperCase());
                    String profile = m.group(2);
                    ToolConfiguration<?> conf = null;

                    try {
                        if (tool == null) {
                            logger.warn("[" + m.group(1) + "] is not a SLIPO toolkit component");
                            return;
                        }
                        switch (tool) {
                            case TRIPLEGEO:
                                conf = propertiesConverter.propertiesToValue(pair.getRight(), TriplegeoConfiguration.class);
                                break;
                            case LIMES:
                                conf = propertiesConverter.propertiesToValue(pair.getRight(), LimesConfiguration.class);
                                break;
                            case FAGI:
                                conf = propertiesConverter.propertiesToValue(pair.getRight(), FagiConfiguration.class);
                                break;
                            case DEER:
                                conf = propertiesConverter.propertiesToValue(pair.getRight(), DeerConfiguration.class);
                                break;
                            default:
                                return;
                        }
                        if(!result.containsKey(tool)) {
                            result.put(tool, new HashMap<String, ToolConfiguration<?>>());
                        }
                        if(!result.get(tool).containsKey(profile)) {
                            result.get(tool).put(profile, conf);
                        }
                    } catch (Exception ex) {
                        logger.warn("Failed to parse properties for profile [" + profile + "]", ex);
                    }
                }
            });
        } catch (IOException e) {
            logger.error("Failed to scan classpath for vendor profiles", e);
        }

        postProcessProfiles(result);
        return result;
    }

    private void postProcessProfiles(Map<EnumTool, Map<String, ToolConfiguration<?>>> profiles) {
        for (EnumTool tool : profiles.keySet()) {
            for (ToolConfiguration<?> config : profiles.get(tool).values()) {
                switch (tool) {
                    case TRIPLEGEO:
                        TriplegeoConfiguration typedConfig = (TriplegeoConfiguration) config;
                        typedConfig.setInput("");
                        typedConfig.setClassificationSpec(null);
                        typedConfig.setMappingSpec(null);
                        typedConfig.setOutputDir(null);
                        typedConfig.setTmpDir(null);
                        break;
                    default:
                        continue;
                }
            }
        }
    }

}
