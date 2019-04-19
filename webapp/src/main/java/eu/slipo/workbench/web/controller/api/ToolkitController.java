package eu.slipo.workbench.web.controller.api;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.CaseFormat;

import eu.slipo.workbench.common.model.ApplicationException;
import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.common.model.RestResponse;
import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumOperation;
import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.process.EnumProcessTaskType;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessDefinitionBuilderFactory;
import eu.slipo.workbench.common.model.process.ProcessExecutionRecord;
import eu.slipo.workbench.common.model.process.ProcessExecutionStartException;
import eu.slipo.workbench.common.model.process.ProcessNotFoundException;
import eu.slipo.workbench.common.model.process.ProcessRecord;
import eu.slipo.workbench.common.model.resource.FileSystemDataSource;
import eu.slipo.workbench.common.model.resource.ResourceRecord;
import eu.slipo.workbench.common.model.tool.DeerConfiguration;
import eu.slipo.workbench.common.model.tool.FagiConfiguration;
import eu.slipo.workbench.common.model.tool.LimesConfiguration;
import eu.slipo.workbench.common.model.tool.ToolConfiguration;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;
import eu.slipo.workbench.common.repository.ResourceRepository;
import eu.slipo.workbench.common.service.CatalogUserFileNamingStrategy;
import eu.slipo.workbench.common.service.util.PropertiesConverterService;
import eu.slipo.workbench.web.model.api.ApiErrorCode;
import eu.slipo.workbench.web.model.api.CatalogInput;
import eu.slipo.workbench.web.model.api.EnrichRequest;
import eu.slipo.workbench.web.model.api.FileInput;
import eu.slipo.workbench.web.model.api.FusionRequest;
import eu.slipo.workbench.web.model.api.Input;
import eu.slipo.workbench.web.model.api.InterlinkRequest;
import eu.slipo.workbench.web.model.api.TransformRequest;
import eu.slipo.workbench.web.model.api.process.ProcessExecutionSimpleRecord;
import eu.slipo.workbench.web.model.api.process.TriplegeoApiConfiguration;
import eu.slipo.workbench.web.service.ProcessService;

@Secured({ "ROLE_API" })
@RestController("ApiToolkitController")
@RequestMapping(produces = "application/json")
public class ToolkitController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ToolkitController.class);

    private static DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    private final String vendorDataPath = "common/vendor";

    @Autowired
    private ResourcePatternResolver resourceResolver;

    @Autowired
    private PropertiesConverterService propertiesConverter;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ProcessDefinitionBuilderFactory processDefinitionBuilderFactory;

    @Autowired
    private ProcessService processService;

    /**
     * Enumerate all registered SLIPO Toolkit components profiles
     *
     * @return A dictionary with all profiles per SLIPO Toolkit component
     */
    @GetMapping(value = "/api/v1/toolkit/profiles")
    public RestResponse<?> profiles() {
        try {
            return RestResponse.result(this.getToolProfiles());
        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }
    }

    /**
     * Execute a transformation operation
     *
     * @param request Transform operation configuration
     *
     * @return An instance of {@link ProcessExecutionSimpleRecord} for the new execution
     */
    @PostMapping(value = "/api/v1/toolkit/transform")
    public RestResponse<?> transform(@RequestBody TransformRequest request) {
        ProcessRecord record = null;

        try {
            final TriplegeoApiConfiguration apiConfig = request.getConfiguration();

            final String resourceKey = "1";
            final String procName = String.format("API %s %s", dateFormat.format(new Date()), UUID.randomUUID().toString());

            final TriplegeoConfiguration configuration = (TriplegeoConfiguration) this.getProfileByName(
                EnumTool.TRIPLEGEO, apiConfig.getProfile()
            );

            if(configuration == null) {
                return RestResponse.error(
                    ApiErrorCode.PROFILE_NOT_FOUND,
                    String.format("Profile %s was not found", apiConfig.getProfile())
                );
            }

            apiConfig.merge(configuration);

            ProcessDefinition definition = processDefinitionBuilderFactory.create(procName)
                .description("API Transform Method")
                .transform("transform", stepBuilder -> stepBuilder
                    .group(0)
                    .source(new FileSystemDataSource(request.getPath()))
                    .outputKey(resourceKey)
                    .configuration(configuration))
                .build();

            record = processService.create(definition, EnumProcessTaskType.API);

            return this.start(record, EnumOperation.TRANSFORM);
        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }
    }

    /**
     * Execute an interlink operation
     *
     * @param request Interlink operation configuration
     *
     * @return An instance of {@link ProcessExecutionSimpleRecord} for the new execution
     */
    @PostMapping(value = "/api/v1/toolkit/interlink")
    public RestResponse<?> interlink(@RequestBody InterlinkRequest request) {
        ProcessRecord record = null;

        try {
            final String leftKey = "1";
            final String rightKey = "2";
            final String outputKey = "3";
            final String procName = String.format("API %s %s", dateFormat.format(new Date()), UUID.randomUUID().toString());

            final LimesConfiguration configuration = (LimesConfiguration) this.getProfileByName(EnumTool.LIMES, request.getProfile());

            if(configuration == null) {
                return RestResponse.error(ApiErrorCode.PROFILE_NOT_FOUND, String.format("Profile %s was not found", request.getProfile()));
            }

            ProcessDefinition definition = processDefinitionBuilderFactory.create(procName)
                .description("API Interlink Method")
                .resource("left", leftKey, inputToPath(request.getLeft()), EnumDataFormat.N_TRIPLES)
                .resource("right", rightKey, inputToPath(request.getRight()), EnumDataFormat.N_TRIPLES)
                .interlink("interlink", stepBuilder -> stepBuilder
                    .group(0)
                    .left(leftKey)
                    .right(rightKey)
                    .outputKey(outputKey)
                    .configuration(configuration))
                .build();

            record = processService.create(definition, EnumProcessTaskType.API);

            return this.start(record, EnumOperation.INTERLINK);
        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }
    }

    /**
     * Execute a fusion operation
     *
     * @param request Fusion operation configuration
     *
     * @return An instance of {@link ProcessExecutionSimpleRecord} for the new execution
     */
    @PostMapping(value = "/api/v1/toolkit/fuse")
    public RestResponse<?> fuse(@RequestBody FusionRequest request) {
        ProcessRecord record = null;

        try {
            final String leftKey = "1";
            final String rightKey = "2";
            final String linksKey = "3";
            final String outputKey = "4";
            final String procName = String.format("API %s %s", dateFormat.format(new Date()), UUID.randomUUID().toString());

            final FagiConfiguration configuration = (FagiConfiguration) this.getProfileByName(EnumTool.FAGI, request.getProfile());

            if(configuration == null) {
                return RestResponse.error(ApiErrorCode.PROFILE_NOT_FOUND, String.format("Profile %s was not found", request.getProfile()));
            }
            configuration.setProfile(request.getProfile());
            configuration.setVerbose(true);

            ProcessDefinition definition = processDefinitionBuilderFactory.create(procName)
                .description("API Fuse Method")
                .resource("left", leftKey, inputToPath(request.getLeft()), EnumDataFormat.N_TRIPLES)
                .resource("right", rightKey, inputToPath(request.getRight()), EnumDataFormat.N_TRIPLES)
                .resource("links", linksKey, inputToPath(request.getLinks()), EnumDataFormat.N_TRIPLES)
                .fuse("fuse", stepBuilder -> stepBuilder
                    .group(0)
                    .left(leftKey)
                    .right(rightKey)
                    .link(linksKey)
                    .outputKey(outputKey)
                    .configuration(configuration))
                .build();

            record = processService.create(definition, EnumProcessTaskType.API);

            return this.start(record, EnumOperation.FUSION);
        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }
    }

    /**
     * Execute an enrichment operation
     *
     * @param request Enrichment operation configuration
     *
     * @return An instance of {@link ProcessExecutionSimpleRecord} for the new execution
     */
    @PostMapping(value = "/api/v1/toolkit/enrich")
    public RestResponse<?> enrich(@RequestBody EnrichRequest request) {
        ProcessRecord record = null;

        try {
            final String resourceKey = "1";
            final String outputKey = "2";
            final String procName = String.format("API %s %s", dateFormat.format(new Date()), UUID.randomUUID().toString());

            final DeerConfiguration configuration = (DeerConfiguration) this.getProfileByName(EnumTool.DEER, request.getProfile());

            if(configuration == null) {
                return RestResponse.error(ApiErrorCode.PROFILE_NOT_FOUND, String.format("Profile %s was not found", request.getProfile()));
            }
            configuration.setProfile(request.getProfile());

            ProcessDefinition definition = processDefinitionBuilderFactory.create(procName)
                .description("API Enrich Method")
                .resource("input", resourceKey, inputToPath(request.getInput()), EnumDataFormat.N_TRIPLES)
                .enrich("enrich", stepBuilder -> stepBuilder
                    .group(0)
                    .input(resourceKey)
                    .outputKey(outputKey)
                    .configuration(configuration))
                .build();

            record = processService.create(definition, EnumProcessTaskType.API);

            return this.start(record, EnumOperation.ENRICHMENT);
        } catch (Exception ex) {
            return this.exceptionToResponse(ex);
        }
    }

    private RestResponse<?> start(
        ProcessRecord record, EnumOperation operation
    ) throws ProcessNotFoundException, ProcessExecutionStartException, IOException {
        ProcessExecutionRecord execution = processService.start(record.getId(), record.getVersion(), EnumProcessTaskType.API);

        logger.info("A {} operation is submitted as execution #{}", operation.toString(), execution.getId());
        this.processService.log(this.applicationKey(), execution, operation);

        return RestResponse.result(new ProcessExecutionSimpleRecord(execution));
    }

    private Path inputToPath(Input input) throws Exception {
        switch (input.getType()) {
            case FILESYSTEM:
                FileInput fileInput = ((FileInput) input);
                return this.fileNamingStrategy.resolvePath(this.currentUserId(), fileInput.getPath());
            case CATALOG:
                CatalogInput catalogInput = ((CatalogInput) input);
                ResourceRecord record = this.resourceRepository.findOne(catalogInput.getId(), catalogInput.getVersion());
                if(record == null) {
                    throw ApplicationException.fromMessage(ApiErrorCode.RESOURCE_NOT_FOUND, "Resource was not found.");
                }
                return this.fileNamingStrategy.resolveExecutionPath(CatalogUserFileNamingStrategy.SCHEME + "://" + record.getFilePath());
            default:
                throw new Exception("Input type is not supported");
        }
    }

    private ToolConfiguration<?> getProfileByName(EnumTool tool, String profile) {
        try {
            String re = ".*vendor\\/" + tool.name().toLowerCase() + "\\/.*config\\/profiles\\/(.*)\\/(config\\.properties)";
            Pattern pattern = Pattern.compile(re);

            List<Pair<Path, Resource>> profiles = Stream.of(resourceResolver.getResources("classpath:" + vendorDataPath + "/**"))
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
                .collect(Collectors.toList());

            for(Pair<Path, Resource> pair : profiles) {
                Matcher m = pattern.matcher(pair.getLeft().toString());
                if ((m.matches()) && (m.group(1).equals(profile))) {
                    try {
                       switch (tool) {
                            case TRIPLEGEO:
                                return propertiesConverter.propertiesToValue(pair.getRight(), TriplegeoConfiguration.class);
                            case LIMES:
                                return propertiesConverter.propertiesToValue(pair.getRight(), LimesConfiguration.class);
                            case FAGI:
                                return propertiesConverter.propertiesToValue(pair.getRight(), FagiConfiguration.class);
                            case DEER:
                                return propertiesConverter.propertiesToValue(pair.getRight(), DeerConfiguration.class);
                            default:
                                return null;
                        }
                    } catch (Exception ex) {
                        logger.warn("Failed to parse properties for profile [" + profile + "]", ex);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed to scan classpath for vendor profiles", e);
        }

        return null;
    }

    private Map<EnumTool, List<String>> getToolProfiles() {
        Map<EnumTool, List<String>> result = new HashMap<EnumTool, List<String>>();
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
                    String name = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, m.group(1));
                    EnumTool tool = EnumTool.fromName(name);
                    String profile = m.group(2);

                    if (tool == null) {
                        logger.warn("[" + m.group(1) + "] is not a SLIPO toolkit component");
                    } else if (result.containsKey(tool)) {
                        result.get(tool).add(profile);
                    } else {
                        result.put(tool, new ArrayList<String>());
                        result.get(tool).add(profile);
                    }
                }
            });
        } catch (IOException e) {
            logger.error("Failed to scan classpath for vendor profiles", e);
        }

        return result;
    }

    private RestResponse<?> exceptionToResponse(Exception ex) {
        logger.error(ex.getMessage(), ex);
        return exceptionToResponse(ex, Error.EnumLevel.ERROR);
    }

}
