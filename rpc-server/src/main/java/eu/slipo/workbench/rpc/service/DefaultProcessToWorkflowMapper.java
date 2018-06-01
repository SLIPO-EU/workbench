package eu.slipo.workbench.rpc.service;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumOperation;
import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.Step;
import eu.slipo.workbench.common.model.resource.DataSource;
import eu.slipo.workbench.common.model.resource.EnumDataSourceType;
import eu.slipo.workbench.common.model.resource.FileSystemDataSource;
import eu.slipo.workbench.common.model.resource.ResourceIdentifier;
import eu.slipo.workbench.common.model.resource.ResourceMetadataCreate;
import eu.slipo.workbench.common.model.resource.ResourceRecord;
import eu.slipo.workbench.common.model.resource.UploadDataSource;
import eu.slipo.workbench.common.model.resource.UrlDataSource;
import eu.slipo.workbench.common.model.tool.AnyTool;
import eu.slipo.workbench.common.model.tool.FagiConfiguration;
import eu.slipo.workbench.common.model.tool.ImportDataConfiguration;
import eu.slipo.workbench.common.model.tool.LimesConfiguration;
import eu.slipo.workbench.common.model.tool.RegisterToCatalogConfiguration;
import eu.slipo.workbench.common.model.tool.ToolConfiguration;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;
import eu.slipo.workbench.common.model.tool.output.EnumImportDataOutputPart;
import eu.slipo.workbench.common.model.tool.output.EnumOutputType;
import eu.slipo.workbench.common.model.tool.output.InputToOutputNameMapper;
import eu.slipo.workbench.common.model.tool.output.OutputPart;
import eu.slipo.workbench.common.repository.ResourceRepository;
import eu.slipo.workbench.common.service.UserFileNamingStrategy;
import eu.slipo.workbench.common.service.util.ClonerService;
import eu.slipo.workbench.common.service.util.PropertiesConverterService;
import eu.slipo.workflows.Workflow;
import eu.slipo.workflows.Workflow.JobDefinitionBuilder;
import eu.slipo.workflows.WorkflowBuilderFactory;
import eu.slipo.workflows.util.digraph.DependencyGraph;
import eu.slipo.workflows.util.digraph.DependencyGraphs;
import eu.slipo.workflows.util.digraph.TopologicalSort.CycleDetected;

@Service
public class DefaultProcessToWorkflowMapper implements ProcessToWorkflowMapper
{
    private static final Logger logger = LoggerFactory.getLogger(DefaultProcessToWorkflowMapper.class);

    @Autowired
    private WorkflowBuilderFactory workflowBuilderFactory;

    @Autowired
    private PropertiesConverterService propertiesConverter;

    @Autowired
    @Qualifier("defaultUserFileNamingStrategy")
    private UserFileNamingStrategy userFileNamingStrategy;

    @Autowired
    @Qualifier("catalogDataDirectory")
    private Path catalogDataDir;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    @Qualifier("triplegeo.flow")
    private Flow triplegeoFlow;

    @Autowired
    @Qualifier("triplegeo.configurationProfiles")
    private Map<String, TriplegeoConfiguration> triplegeoConfigurationProfiles;

    @Autowired
    @Qualifier("limes.flow")
    private Flow limesFlow;

    @Autowired
    @Qualifier("fagi.flow")
    private Flow fagiFlow;

    @Autowired
    @Qualifier("registerResource.flow")
    private Flow registerResourceFlow;

    @Autowired
    @Qualifier("downloadFile.flow")
    private Flow downloadFileFlow;

    @Value("${slipo.rpc-server.workflows.salt-for-identifier:1}")
    private Long salt;

    private static class ProcessDefinitionDependencyAnalyzer
    {
        private final ProcessDefinition definition;

        public ProcessDefinitionDependencyAnalyzer(ProcessDefinition definition)
        {
            this.definition = definition;
        }

        /**
         * Iterate on steps in topological order.
         * @throws CycleDetected
         */
        public Iterable<Step> stepsInTopologicalOrder()
            throws CycleDetected
        {
            final List<Step> steps = definition.steps();

            // Build dependency graph

            final DependencyGraph dependencyGraph = DependencyGraphs.create(steps.size());
            for (Step step: steps) {
                for (Integer inputKey: step.inputKeys()) {
                    Step dependency = definition.stepByResourceKey(inputKey);
                    if (dependency != null)
                        dependencyGraph.addDependency(step.key(), dependency.key());
                }
            }

            final Iterable<Integer> keysInTopologicalOrder =
                DependencyGraphs.topologicalSort(dependencyGraph);

            return Iterables.transform(keysInTopologicalOrder, k -> definition.stepByKey(k));
        }
    }

    @Override
    public Workflow buildWorkflow(long id, long version, ProcessDefinition definition, int createdBy)
        throws CycleDetected
    {
        Assert.notNull(definition, "A process definition is required!");
        final UUID workflowId = computeWorkflowId(id, version);
        final Workflow.Builder workflowBuilder = workflowBuilderFactory.get(workflowId);
        Workflow workflow = buildWorkflow(workflowBuilder, definition, createdBy);
        return workflow;
    }

    @Override
    public UUID computeWorkflowId(long id, long version)
    {
        byte[] data = ByteBuffer.wrap(new byte[3 * Long.BYTES])
            .putLong(id).putLong(version).putLong(salt)
            .array();

        return UUID.nameUUIDFromBytes(data);
    }

    private Workflow buildWorkflow(Workflow.Builder workflowBuilder, ProcessDefinition definition, int createdBy)
        throws CycleDetected
    {
        // Map external sources to the name of the node (assigned task to carry out downloading)
        final Map<DataSource, String> sourceToNodeName = new HashMap<>();

        // Map a pair of (nodeName, part) to an output path (if any).
        final Table<String, OutputPart<? extends AnyTool>, Path> nodeNameToOutputNames = HashBasedTable.create();

        // Map a node's alias to a real name (i.e. to the one know to the workflow level)
        final Map<String, String> nodeAliasToNodeName = new HashMap<>();

        // Examine referenced sources.
        // A data source of URL is handled by adding an importer node that will download the
        // resource (to make it available to other workflow nodes).

        final Set<DataSource> sourcesToDownload = definition.steps().stream()
            .flatMap(step -> step.sources().stream())
            .filter(UrlDataSource.class::isInstance)
            .collect(Collectors.toSet());

        for (DataSource source: sourcesToDownload) {
            final URL url = ((UrlDataSource) source).getUrl();
            final String nodeName = "download-" + Integer.toHexString(source.hashCode());
            final String outputName = (new ImportDataConfiguration(url)).getOutputName();
            // Add a job node (a downloader) into workflow
            workflowBuilder.job(b -> b.name(nodeName)
                .flow(downloadFileFlow)
                .parameters(
                    p -> p.addString("url", url.toString()).addString("outputName", outputName))
                .output(outputName));
            // Map this source to a node name
            sourceToNodeName.put(source, nodeName);
            // Map node to output (a single entry under the default key)
            Path outputPath = Paths.get(outputName);
            nodeNameToOutputNames.put(nodeName, EnumImportDataOutputPart.DOWNLOAD, outputPath);
        }

        //
        // Map each processing step to a job node inside the workflow
        //

        final ProcessDefinitionDependencyAnalyzer dependencyAnalyzedDefinition =
            new ProcessDefinitionDependencyAnalyzer(definition);

        for (Step step: dependencyAnalyzedDefinition.stepsInTopologicalOrder()) {
            final EnumTool tool = step.tool();
            final ToolConfiguration<? extends AnyTool> configuration = step.configuration();
            final List<Step.Input> input = step.input();

            final JobDefinitionBuilder jobDefinitionBuilder = JobDefinitionBuilder.create(step.nodeName());

            // Define inputs for this job node

            List<String> inputNames = new ArrayList<>();
            if (!step.sources().isEmpty()) {
                // This step expects input from an imported data source (external to the application)
                Assert.state(input.isEmpty(), "Expected no input keys for this step");
                for (DataSource source: step.sources()) {
                    String dependencyName = sourceToNodeName.get(source);
                    if (dependencyName != null) {
                        Path inputName =
                            nodeNameToOutputNames.get(dependencyName, EnumImportDataOutputPart.DOWNLOAD);
                        Assert.state(inputName != null, "No output produced by the step we depend on");
                        jobDefinitionBuilder.input(dependencyName, inputName);
                        inputNames.add(inputName.toString());
                    } else {
                        Path inputPath = resolveToPath(source, createdBy);
                        jobDefinitionBuilder.input(inputPath);
                        inputNames.add(inputPath.getFileName().toString());
                    }
                }
            } else {
                // This step expects input from a catalog resource or from another step
                for (Step.Input p: step.input()) {
                    ResourceIdentifier resourceIdentifier =
                        definition.resourceIdentifierByResourceKey(p.inputKey());
                    if (resourceIdentifier != null) {
                        // The input is a registered file resource
                        Path inputPath = resolveToPath(resourceIdentifier);
                        jobDefinitionBuilder.input(inputPath);
                        inputNames.add(inputPath.getFileName().toString());
                    } else {
                        // The input is the output of another step we depend on
                        Step dependency = definition.stepByResourceKey(p.inputKey());
                        Assert.state(dependency != null,
                            "A step is expected to produce the input we depend on!");
                        OutputPart<? extends AnyTool> part = dependency.outputPart(p.partKey());
                        Assert.state(part != null, "No output part matching given key!");
                        String dependencyName = dependency.nodeName();
                        dependencyName = nodeAliasToNodeName.getOrDefault(dependencyName, dependencyName);
                        Path inputName = nodeNameToOutputNames.get(dependencyName, part);
                        Assert.state(inputName != null, "No output produced by the step we depend on");
                        jobDefinitionBuilder.input(dependencyName, inputName);
                        inputNames.add(inputName.toString());
                    }
                }
            }

            // Define output names

            final InputToOutputNameMapper<? extends AnyTool> outputNameMapper = configuration.getOutputNameMapper();
            final Map<OutputPart<? extends AnyTool>, Path> outputNames = outputNameMapper.apply(inputNames)
                .entries().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> Paths.get(e.getValue())));
            if (!outputNames.isEmpty()) {
                jobDefinitionBuilder.output(outputNames.values());
                nodeNameToOutputNames.row(step.nodeName()).putAll(outputNames);
            }

            // Define flow and parameters
            // Note that a flow may be null (in such a case it represents a no-op step)

            Flow flow = null;
            Properties parametersMap = null;

            switch (tool) {
            case IMPORTER:
                {
                    // This is a no-op: an importer step only exists to link to downloader node
                    Assert.state(inputNames.size() == 1, "An import step expects a single input");
                    parametersMap = null;
                    flow = null;
                    // Link to the downloader node responsible for our source URL
                    final URL url = ((ImportDataConfiguration) configuration).getUrl();
                    Assert.state(url != null, "An importer node should target a non-empty URL");
                    final DataSource source = sourcesToDownload.stream()
                        .filter(s -> url.equals(((UrlDataSource) s).getUrl()))
                        .findFirst().get();
                    nodeAliasToNodeName.put(step.nodeName(), sourceToNodeName.get(source));
                }
                break;
            case REGISTER:
                {
                    Assert.state(input.size() == 1, "A registration step expects a single input");
                    Step producer = definition.stepByResourceKey(input.get(0).inputKey());
                    parametersMap = buildParameters(
                        definition, (RegisterToCatalogConfiguration) configuration, producer, createdBy);
                    flow = registerResourceFlow;
                }
                break;
            case TRIPLEGEO:
                {
                    Assert.state(inputNames.size() == 1, "A transformation step expects a single input");
                    parametersMap = buildParameters(definition, (TriplegeoConfiguration) configuration, createdBy);
                    flow = triplegeoFlow;
                }
                break;
            case LIMES:
                {
                    Assert.state(inputNames.size() == 2, "A interlinking step expects a pair (L, R) of inputs");
                    parametersMap = buildParameters(definition, configuration, createdBy);
                    flow = limesFlow;
                }
                break;
            case FAGI:
                {
                    Assert.state(inputNames.size() == 3, "A fusion step expects a triplet (L, R, links) of inputs");
                    parametersMap = buildParameters(definition, (FagiConfiguration) configuration, createdBy);
                    flow = fagiFlow;
                }
                break;
            case DEER:
                throw new NotImplementedException("Α Batch flow for a tool of type [" + tool + "]");

            default:
                Assert.state(false, "Did not expect a tool of type [" + tool + "]");
            }

            // Add the job node (mapped from this processing step) into workflow

            if (flow != null) {
                jobDefinitionBuilder.flow(flow).parameters(parametersMap);
                workflowBuilder.job(jobDefinitionBuilder.build());
            }
        }

        return workflowBuilder.build();
    }

    private Properties buildParameters(ProcessDefinition def, ToolConfiguration<? extends AnyTool> config, int userId)
    {
        return propertiesConverter.valueToProperties(config);
    }

    private Properties buildParameters(
        ProcessDefinition def, RegisterToCatalogConfiguration config, Step producer, int userId)
    {
        final Properties parametersMap = new Properties();

        EnumDataFormat format = producer.outputFormat();
        EnumDataFormat inputFormat = producer.configuration().getInputFormat();

        parametersMap.put("createdBy", Integer.valueOf(userId).longValue());
        parametersMap.put("format", format.toString());
        parametersMap.put("inputFormat", inputFormat.toString());
        parametersMap.put("processName", def.name());

        ResourceMetadataCreate metadata = config.getMetadata();
        parametersMap.put("name", metadata.getName());
        if (metadata.getDescription() != null) {
            parametersMap.put("description", metadata.getDescription());
        }

        ResourceIdentifier target = config.getTarget();
        if (target != null) {
            parametersMap.put("resourceId", target.getId());
        }

        return parametersMap;
    }

    private Properties buildParameters(ProcessDefinition def, TriplegeoConfiguration config, int userId)
    {
        final Properties parametersMap = propertiesConverter.valueToProperties(config);

        // If originates from a configuration profile, draw missing parts from profile

        final String profileName = parametersMap.getProperty("profile");
        if (!StringUtils.isEmpty(profileName)) {
            TriplegeoConfiguration profile = triplegeoConfigurationProfiles.get(profileName.toLowerCase());
            if (profile == null)
                throw new NoSuchElementException(
                    "No such profile for Triplegeo configuration: [" + profileName + "]");

            // Use profile defaults to draw (missing) properties

            if (StringUtils.isEmpty(parametersMap.getProperty("mappingSpec")))
                parametersMap.put("mappingSpec", profile.getMappingSpec());

            if (StringUtils.isEmpty(parametersMap.getProperty("classificationSpec")))
                parametersMap.put("classificationSpec", profile.getClassificationSpec());

            // Clear profile (everything useful is expanded into parameters)
            parametersMap.remove("profile");
        }

        // This configuration contains references to files (`mappingSpec`, `classificationSpec`)
        // that may need to be resolved to absolute URIs.

        for (String key: Arrays.asList("mappingSpec", "classificationSpec")) {
            final String location = parametersMap.getProperty(key);
            if (StringUtils.isEmpty(location))
                continue;
            URI uri = resolveToAbsoluteUri(location, userId);
            parametersMap.put(key, uri.toString());
        }

        return parametersMap;
    }

    private Properties buildParameters(ProcessDefinition def, FagiConfiguration config, int userId)
    {
        final Properties parametersMap = propertiesConverter.valueToProperties(config);

        // If this configuration contains a reference to `rulesSpec` file, then this may need to
        // be resolved to an absolute URI

        String rulesLocation = parametersMap.getProperty("rulesSpec");
        Assert.state(!StringUtils.isEmpty(rulesLocation), "Expected a location for rules specification file");
        URI rulesUri = resolveToAbsoluteUri(rulesLocation, userId);
        parametersMap.put("rulesSpec", rulesUri.toString());

        return parametersMap;
    }

    /**
     * Resolve a resource identifier (an instance of {@link ResourceIdentifier}) into a
     * resource record.
     *
     * @param resourceIdentifier A pair of id and version
     * @return a full-fledged record as returned from resource repository
     *
     * @throws NoSuchElementException if the pair of (id,version) does not correspond to
     *   a registered resource.
     */
    private ResourceRecord findResourceRecord(ResourceIdentifier resourceIdentifier)
    {
        Assert.state(resourceIdentifier != null, "Expected a non-null resource identifier");

        final long id = resourceIdentifier.getId(), version = resourceIdentifier.getVersion();
        Assert.state(id > 0, "The resource id is not valid");
        Assert.state(version > 0, "The resource version is not valid");

        ResourceRecord record = resourceRepository.findOne(id, version);
        if (record == null)
            throw new NoSuchElementException(
                String.format("The resource does not exist (id=%d, version=%d)", id, version));

        return record;
    }

    private URI resolveToAbsoluteUri(String uri, int userId)
    {
        Assert.isTrue(!StringUtils.isEmpty(uri), "A non-empty URI is required");
        return resolveToAbsoluteUri(URI.create(uri), userId);
    }

    private URI resolveToAbsoluteUri(URI uri, int userId)
    {
        Assert.notNull(uri, "A non-empty URI is required");
        if (!uri.isAbsolute()) {
            Path path = Paths.get(uri.getPath());
            Assert.notNull(path, "The input URI is expected to have a non-empty path");
            if (!path.isAbsolute()) {
                // Treat as a file resource into user's data directory
                path = userFileNamingStrategy.resolvePath(userId, path);
            }
            // Convert to a file URI
            uri = path.toUri();
        }
        return uri;
    }

    private Path resolveToPath(ResourceIdentifier resourceIdentifier)
    {
        ResourceRecord record = findResourceRecord(resourceIdentifier);
        return resolveToPath(record);
    }

    private Path resolveToPath(ResourceRecord record)
    {
        Assert.state(record != null, "Expected a non-null record");

        final String p = record.getFilePath();
        Assert.state(!StringUtils.isEmpty(p), "Expected a non-empty relative path");

        return catalogDataDir.resolve(p);
    }

    private Path resolveToPath(DataSource source, int userId)
    {
        Assert.state(source != null, "Expected a non-null source");

        Path path = null;

        final EnumDataSourceType type = source.getType();
        switch (type) {
        case UPLOAD:
            {
                path = Paths.get(((UploadDataSource) source).getPath());
                path = userFileNamingStrategy.resolvePath(userId, path);
            }
            break;
        case FILESYSTEM:
            {
                path = Paths.get(((FileSystemDataSource) source).getPath());
                path = userFileNamingStrategy.resolvePath(userId, path);
            }
            break;
        case URL:
            throw new UnsupportedOperationException(
                "A data source of [" + type + "] cannot be resolved to a local file path");
        case HARVESTER:
            throw new NotImplementedException(
                "A data source of type [" + type + "] is not supported (yet)");
        default:
            Assert.state(false, "Did not expect a source type of type [" + type + "]");
        }

        return path;
    }

}
