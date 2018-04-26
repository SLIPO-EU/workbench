package eu.slipo.workbench.rpc.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.google.common.collect.Iterables;

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
import eu.slipo.workbench.common.model.tool.LimesConfiguration;
import eu.slipo.workbench.common.model.tool.MetadataRegistrationConfiguration;
import eu.slipo.workbench.common.model.tool.ToolConfiguration;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;
import eu.slipo.workbench.common.repository.ResourceRepository;
import eu.slipo.workbench.common.service.FileNamingStrategy;
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
    @Autowired
    private WorkflowBuilderFactory workflowBuilderFactory;

    @Autowired
    private PropertiesConverterService propertiesConverter;

    @Autowired
    @Qualifier("userDataDirectory")
    private Path userDataDir;

    @Autowired
    @Qualifier("defaultFileNamingStrategy")
    private FileNamingStrategy userDataNamingStrategy;

    @Autowired
    @Qualifier("catalogDataDirectory")
    private Path catalogDataDir;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    @Qualifier("triplegeo.flow")
    private Flow triplegeoFlow;

    @Autowired
    @Qualifier("limes.flow")
    private Flow limesFlow;

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

    /**
     * Extract an output name for data downloaded from a URL. This name will be suitable to be
     * used as the file name (under which data will be saved).
     *
     * @param url A downloadable public URL
     * @param useFragment If the URL reference (i.e. fragment) should be used as part of the output
     *   name
     */
    private String extractOutputName(URL url, boolean useFragment)
    {
        String fileName = Paths.get(url.getPath()).getFileName().toString();
        String fragment = url.getRef();
        if (!useFragment || fragment == null)
            return fileName;

        String name = StringUtils.stripFilenameExtension(fileName);
        String extension = fileName.substring(1 + name.length());
        return String.format("%s-%s.%s", name, fragment, extension);
     }

    private Workflow buildWorkflow(Workflow.Builder workflowBuilder, ProcessDefinition definition, int createdBy)
        throws CycleDetected
    {
        final Map<DataSource, String> sourceToNodeName = new HashMap<>();
        final Map<String, String> nodeNameToOutputName = new HashMap<>();

        // Examine referenced sources.
        // A data source of URL is handled by adding a job node that will download the
        // resource (to make it available to other workflow nodes).

        final Set<DataSource> sourcesToDownload = definition.steps().stream()
            .flatMap(step -> step.sources().stream())
            .filter(source -> source instanceof UrlDataSource)
            .collect(Collectors.toSet());

        for (DataSource source: sourcesToDownload) {
            final URL url = ((UrlDataSource) source).getUrl();
            final String nodeName = "download-" + Integer.toHexString(source.hashCode());
            final String outputName = extractOutputName(url, true);
            // Add a job node into workflow
            workflowBuilder.job(b -> b.name(nodeName)
                .flow(downloadFileFlow)
                .parameters(
                    p -> p.addString("url", url.toString()).addString("outputName", outputName))
                .output(outputName));
            // Map this source to a node name
            sourceToNodeName.put(source, nodeName);
            nodeNameToOutputName.put(nodeName, outputName);
        }

        // Map each processing step to a job node inside the workflow

        final ProcessDefinitionDependencyAnalyzer dependencyAnalyzedDefinition =
            new ProcessDefinitionDependencyAnalyzer(definition);
        for (Step step: dependencyAnalyzedDefinition.stepsInTopologicalOrder()) {
            EnumTool tool = step.tool();
            List<Integer> inputKeys = step.inputKeys();
            JobDefinitionBuilder jobDefinitionBuilder = JobDefinitionBuilder.create(step.nodeName());

            // Define inputs for this job node

            List<String> inputNames = new ArrayList<>();
            if (!step.sources().isEmpty()) {
                // This step imports data from a data source (external to the application)
                Assert.state(inputKeys.isEmpty(), "Expected no input keys for this step");
                for (DataSource source: step.sources()) {
                    String dependencyName = sourceToNodeName.get(source);
                    if (dependencyName != null) {
                        String inputName = nodeNameToOutputName.get(dependencyName);
                        Assert.state(inputName != null,
                            "No output is known to be produced by the step we depend on!");
                        jobDefinitionBuilder.input(dependencyName, inputName);
                        inputNames.add(inputName);
                    } else {
                        Path inputPath = resolveToPath(source, createdBy);
                        jobDefinitionBuilder.input(inputPath);
                        inputNames.add(inputPath.getFileName().toString());
                    }
                }
            } else {
                // This step expects input from a catalog resource or from another step
                Assert.state(!inputKeys.isEmpty(), "Did not expect an empty list of input keys");
                for (Integer inputKey: inputKeys) {
                    ResourceIdentifier resourceIdentifier =
                        definition.resourceIdentifierByResourceKey(inputKey);
                    if (resourceIdentifier != null) {
                        // The input is a registered file resource
                        Path inputPath = resolveToPath(resourceIdentifier);
                        jobDefinitionBuilder.input(inputPath);
                        inputNames.add(inputPath.getFileName().toString());
                    } else {
                        // The input is the output of another step we depend on
                        Step dependency = definition.stepByResourceKey(inputKey);
                        Assert.state(dependency != null,
                            "A step is expected to produce the input we depend on!");
                        String inputName = nodeNameToOutputName.get(dependency.nodeName());
                        Assert.state(inputName != null,
                            "No output is known to be produced by the step we depend on!");
                        jobDefinitionBuilder.input(dependency.nodeName(), inputName);
                        inputNames.add(inputName);
                    }
                }
            }

            // Define flow, parameters, and output name

            String outputName = null;
            switch (tool) {
            case REGISTER:
                {
                    Assert.state(inputKeys.size() == 1, "A registration step expects a single input");
                    Step producer = definition.stepByResourceKey(inputKeys.get(0));
                    MetadataRegistrationConfiguration configuration =
                        (MetadataRegistrationConfiguration) step.configuration();
                    Properties parametersMap =
                        buildParameters(definition, configuration, producer, createdBy);
                    jobDefinitionBuilder
                        .flow(registerResourceFlow)
                        .parameters(parametersMap);
                }
                break;
            case TRIPLEGEO:
                {
                    Assert.state(inputNames.size() == 1, "A transformation step expects a single input");
                    TriplegeoConfiguration configuration = (TriplegeoConfiguration) step.configuration();
                    EnumDataFormat outputFormat = configuration.getOutputFormat();
                    outputName = StringUtils.stripFilenameExtension(inputNames.get(0)) + "."
                        + outputFormat.getFilenameExtension();
                    Properties parametersMap = buildParameters(definition, configuration, createdBy);
                    jobDefinitionBuilder
                        .flow(triplegeoFlow)
                        .parameters(parametersMap)
                        .output(outputName);
                }
                break;
            case LIMES:
                {
                    Assert.state(inputNames.size() == 2, "A interlinking step expects a pair of inputs");
                    LimesConfiguration configuration = (LimesConfiguration) step.configuration();
                    Properties parametersMap = buildParameters(definition, configuration, createdBy);
                    outputName = "accepted.nt";
                    jobDefinitionBuilder
                        .flow(limesFlow)
                        .parameters(parametersMap)
                        .output(outputName);
                }
                break;
            case DEER:
            case FAGI:
                throw new NotImplementedException("Α Batch flow for a tool of type [" + tool + "]");
            default:
                Assert.state(false, "Did not expect a tool of type [" + tool + "]");
            }

            // Set output name for this job node
            if (outputName != null)
                nodeNameToOutputName.put(step.nodeName(), outputName);

            // Add the job node mapping to this processing step
            workflowBuilder.job(jobDefinitionBuilder.build());
        }

        return workflowBuilder.build();
    }

    private Properties buildParameters(
        ProcessDefinition def, MetadataRegistrationConfiguration config, Step producer, int userId)
    {
        final Properties parametersMap = new Properties();

        EnumDataFormat format = producer.outputFormat();
        ToolConfiguration producerConfiguration = producer.configuration();

        EnumDataFormat inputFormat = EnumDataFormat.N_TRIPLES;
        if (producer.operation() == EnumOperation.TRANSFORM
                && (producerConfiguration instanceof TriplegeoConfiguration))
        {
            inputFormat = ((TriplegeoConfiguration) producerConfiguration).getInputFormat();
        }

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

    private Properties buildParameters(
        ProcessDefinition def, TriplegeoConfiguration config, int userId)
    {
        final Properties parametersMap = propertiesConverter.valueToProperties(config);

        // Todo Load and override a configuration profile (if such a thing is given)

        // This configuration contains references to files (mappingSpec, classificationSpec)
        // that may need to be resolved to absolute file paths.

        for (String key: Arrays.asList("mappingSpec", "classificationSpec")) {
            String location = parametersMap.getProperty(key);
            if (StringUtils.isEmpty(location))
                continue;
            if (location.startsWith("/")
                    || location.startsWith("classpath:") || location.startsWith("file:"))
                continue; // do not touch if given as a URI or as an absolute path
            // Assume this is a file resource into user's data directory
            Path path = userDataNamingStrategy.resolvePath(userId, location);
            parametersMap.put(key, path.toUri().toString());
        }

        return parametersMap;
    }

    private Properties buildParameters(
        ProcessDefinition def, LimesConfiguration config, int userId)
    {
        return propertiesConverter.valueToProperties(config);
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
                path = userDataNamingStrategy.resolvePath(userId, path);
            }
            break;
        case FILESYSTEM:
            {
                path = Paths.get(((FileSystemDataSource) source).getPath());
                path = userDataNamingStrategy.resolvePath(userId, path);
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
