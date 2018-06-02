package eu.slipo.workbench.common.model.process;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumOperation;
import eu.slipo.workbench.common.model.poi.EnumResourceType;
import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.process.Step.Input;
import eu.slipo.workbench.common.model.resource.DataSource;
import eu.slipo.workbench.common.model.resource.ResourceIdentifier;
import eu.slipo.workbench.common.model.resource.ResourceMetadataCreate;
import eu.slipo.workbench.common.model.resource.UrlDataSource;
import eu.slipo.workbench.common.model.tool.AnyTool;
import eu.slipo.workbench.common.model.tool.FuseConfiguration;
import eu.slipo.workbench.common.model.tool.FuseTool;
import eu.slipo.workbench.common.model.tool.ImportDataConfiguration;
import eu.slipo.workbench.common.model.tool.InterlinkConfiguration;
import eu.slipo.workbench.common.model.tool.InterlinkTool;
import eu.slipo.workbench.common.model.tool.RegisterToCatalogConfiguration;
import eu.slipo.workbench.common.model.tool.ToolConfiguration;
import eu.slipo.workbench.common.model.tool.TransformConfiguration;
import eu.slipo.workbench.common.model.tool.TransformTool;
import eu.slipo.workbench.common.model.tool.output.EnumOutputType;
import eu.slipo.workbench.common.service.util.ClonerService;

/**
 * A builder for a {@link ProcessDefinition}
 */
public class ProcessDefinitionBuilder 
{
    public static abstract class StepBuilder
    {
        protected abstract Step build();
    }
    
    /**
     * A builder for a generic {@link Step} inside a process.
     */
    public class GenericStepBuilder extends StepBuilder
    {        
        private final Supplier<Step> stepFactory;
        
        protected final int key;

        protected final String name;

        protected String nodeName;
        
        protected int group = 0;
        
        protected EnumOperation operation;

        protected EnumTool tool;

        protected List<Input> input = new ArrayList<>();

        protected List<DataSource> sources = new ArrayList<>();

        protected Integer outputKey;
        
        protected EnumDataFormat outputFormat;

        protected ToolConfiguration<? extends AnyTool> configuration;

        /**
         * Create a builder for a step ({@link Step}).
         *
         * @param key A unique (across its process) key for this step
         * @param name A name (preferably unique) for this step.
         * @param stepFactory A factory to create instances of {@link Step}
         */
        protected GenericStepBuilder(int key, String name, Supplier<Step> stepFactory)
        {
            Assert.isTrue(!StringUtils.isEmpty(name), "Expected a name for this step");
            Assert.notNull(stepFactory, "A step factory is required");
            this.key = key;
            this.name = name;
            this.nodeName = Step.slugifyName(name);
            this.stepFactory = stepFactory;
        }

        protected GenericStepBuilder(int key, String name)
        {
            this(key, name, Step::new);
        }
        
        public GenericStepBuilder nodeName(String nodeName)
        {
            Assert.notNull(nodeName, "A name is required");
            Assert.isTrue(nodeName.matches("^[a-zA-Z][-\\w]*$"), "The name is invalid");
            this.nodeName = nodeName;
            return this;
        }
        
        /**
         * Set the operation type.
         * @param operation
         */
        public GenericStepBuilder operation(EnumOperation operation)
        {
            Assert.notNull(operation, "Expected an non-null operation");
            this.operation = operation;
            return this;
        }

        public GenericStepBuilder group(int groupNumber)
        {
            this.group = groupNumber;
            return this;
        }
        
        /**
         * Set the tool that implements the step operation
         * @param tool
         */
        public GenericStepBuilder tool(EnumTool tool)
        {
            Assert.notNull(tool, "Expected a tool constant");
            this.tool = tool;
            return this;
        }

        /**
         * Provide the tool-specific configuration
         * @param configuration A tool-specific configuration bean
         */
        
        public GenericStepBuilder configuration(ToolConfiguration<? extends AnyTool> configuration)
        {
            Assert.notNull(configuration, "Expected a non-null configuration");
            
            final EnumDataFormat expectedOutputFormat = configuration.getOutputFormat();
            Assert.isTrue(outputFormat == null || expectedOutputFormat == null || 
                    outputFormat == expectedOutputFormat, 
                "The output format must agree with the one reported by step configuration");
            Assert.isTrue(tool == null || tool == configuration.getTool(), 
                "The tool must agree with one reported by step configuration");
            
            // Make a copy of this configuration
            
            try {
                @SuppressWarnings("unchecked")
                ToolConfiguration<? extends AnyTool> clonedConfiguration = 
                    (ToolConfiguration<? extends AnyTool>) cloner.cloneAsBean(configuration);
                this.configuration = clonedConfiguration;
            } catch (Exception ex) {
                throw new IllegalStateException("Cannot clone the configuration object", ex);
            }
            
            // A couple of things are implied from tool's configuration
            this.tool = configuration.getTool();
            this.outputFormat = expectedOutputFormat;
            
            return this;
        }

        /**
         * Assign a unique key to the resource generated as output of this step inside
         * a process.
         * @param outputKey
         */
        public GenericStepBuilder outputKey(int outputKey)
        {
            this.outputKey = outputKey;
            return this;
        }

        /**
         * Designate a process-wide resource as an input to this step
         * 
         * @param inputKey The key identifying the resource
         */
        public GenericStepBuilder input(int inputKey)
        {
            this.input.add(Input.of(inputKey));
            return this;
        }
        
        /**
         * Designate a part of a process-wide resource as an input to this step. This is only meaningful
         * for resources produced as outputs of other steps (represented as {@link ProcessOutput}).
         * 
         * @param inputKey The key identifying the resource
         * @param partKey The key identifying a part of the input
         */
        public GenericStepBuilder input(int inputKey, String partKey)
        {
            this.input.add(Input.of(inputKey, partKey));
            return this;
        }
        
        protected GenericStepBuilder input(Input p)
        {
            Assert.notNull(p, "An input descriptor is required");
            this.input.add(p);
            return this;
        }

        /**
         * Set a list of external data sources that should be input to this step.
         *
         * <p>A {@link DataSource} is an input that is external to the application, i.e it is
         * neither a catalog resource nor a intermediate result of the (enclosing) process.
         *
         * @param s A list of data sources
         */
        public GenericStepBuilder source(List<DataSource> s)
        {
            Assert.notNull(s, "A list of data sources is required");
            this.sources.addAll(s);
            return this;
        }

        /**
         * @see {@link Step.GenericStepBuilder#source(List)}
         * @param s
         */
        public GenericStepBuilder source(DataSource s)
        {
            Assert.notNull(s, "A data source is required");
            this.sources.add(s);
            return this;
        }
        
        /**
         * @see {@link Step.GenericStepBuilder#source(List)}
         * @param s
         */
        public GenericStepBuilder source(DataSource s1, DataSource s2)
        {
            Assert.notNull(s1, "Expected a non-null data source (s1)");
            Assert.notNull(s2, "Expected a non-null data source (s2)");
            this.sources.add(s1);
            this.sources.add(s2);
            return this;
        }
        
        public GenericStepBuilder outputFormat(EnumDataFormat outputFormat)
        {
            Assert.isTrue(configuration == null || outputFormat == configuration.getOutputFormat(), 
                "The output format is different from the one reported by step configuration");
            this.outputFormat = outputFormat;
            return this;
        }

        protected Step build()
        {
            Assert.state(this.operation != null, "The operation must be specified");
            Assert.state(this.tool != null, "The tool must be specified");
            Assert.state(this.configuration != null, "The tool configuration must be provided");
            Assert.state(this.operation == EnumOperation.REGISTER || 
                    (this.outputKey != null && this.outputFormat != null),
                "An output key and format is required for a non-registration step");
            Assert.state(this.tool.supportsOperation(operation), 
                "The operation is not supported by given tool");
            Assert.state(!this.input.isEmpty() || !this.sources.isEmpty(),
                "The list of data sources and list of input keys cannot be both empty!");
            Assert.isTrue(this.outputFormat == null || this.configuration.getOutputFormat() == null || 
                    this.outputFormat == this.configuration.getOutputFormat(), 
                "The output format must agree with the one reported by step configuration");
            
            Step step = this.stepFactory.get();

            step.key = this.key;
            step.group = this.group;
            step.name = this.name;
            step.nodeName = this.nodeName;
            step.operation = this.operation;
            step.tool = this.tool;
            step.sources = new ArrayList<>(this.sources);
            step.input = new ArrayList<>(this.input);
            step.outputKey = this.outputKey;
            step.outputFormat = this.outputFormat;
            step.configuration = this.configuration;
           
            return step;
        }
    }
    
    public class TransformStepBuilder extends StepBuilder
    {
        private final GenericStepBuilder stepBuilder;
        
        protected TransformStepBuilder(int key, String name) 
        {
            this.stepBuilder = 
                ProcessDefinitionBuilder.this.new GenericStepBuilder(key, name, Step::new);
            this.stepBuilder.operation(EnumOperation.TRANSFORM);
            this.stepBuilder.tool(EnumTool.TRIPLEGEO);
        }
        
        public TransformStepBuilder group(int groupNumber)
        {
            this.stepBuilder.group(groupNumber);
            return this;
        }
        
        public TransformStepBuilder nodeName(String nodeName)
        {
            this.stepBuilder.nodeName(nodeName);
            return this;
        }
        
        public TransformStepBuilder source(DataSource source)
        {
            Assert.isTrue(this.stepBuilder.sources.isEmpty() && this.stepBuilder.input.isEmpty(), 
                "A input (either datasource or a key) is already specified for this step");
            this.stepBuilder.source(source);
            return this;
        }
        
        protected TransformStepBuilder input(Input p)
        {
            Assert.isTrue(this.stepBuilder.sources.isEmpty() && this.stepBuilder.input.isEmpty(), 
                "A input (either datasource or a key) is already specified for this step");
            this.stepBuilder.input(p);
            return this;
        }
        
        public TransformStepBuilder input(int inputKey)
        {
            return input(Input.of(inputKey));
        }
        
        public TransformStepBuilder input(int inputKey, String partKey)
        {
            return input(Input.of(inputKey, partKey));
        }
        
        public TransformStepBuilder configuration(TransformConfiguration<? extends TransformTool> configuration)
        {
            this.stepBuilder.configuration(configuration);
            return this;
        }
        
        public TransformStepBuilder outputKey(int outputKey)
        {
            this.stepBuilder.outputKey(outputKey);
            return this;
        }
        
        public TransformStepBuilder outputFormat(EnumDataFormat format)
        {
            this.stepBuilder.outputFormat(format);
            return this;
        }
        
        @Override
        protected Step build()
        {
            return this.stepBuilder.build();
        }
    }
    
    public class InterlinkStepBuilder extends StepBuilder
    {
        private final GenericStepBuilder stepBuilder;
        
        private Input left;
        
        private Input right;

        public InterlinkStepBuilder(int key, String name)
        {
            this.stepBuilder = 
                ProcessDefinitionBuilder.this.new GenericStepBuilder(key, name, Step::new);
            this.stepBuilder.operation(EnumOperation.INTERLINK);
            this.stepBuilder.tool(EnumTool.LIMES);
        }
        
        public InterlinkStepBuilder group(int groupNumber)
        {
            this.stepBuilder.group(groupNumber);
            return this;
        }
        
        public InterlinkStepBuilder nodeName(String nodeName)
        {
            this.stepBuilder.nodeName(nodeName);
            return this;
        }
        
        public InterlinkStepBuilder left(int inputKey)
        {
            Assert.isTrue(this.left == null, "The left input is already specified");
            this.left = Input.of(inputKey);
            return this;
        }
        
        public InterlinkStepBuilder left(int inputKey, String partKey)
        {
            Assert.isTrue(this.left == null, "The left input is already specified");
            this.left = Input.of(inputKey, partKey);
            return this;
        }
        
        public InterlinkStepBuilder right(int inputKey)
        {
            Assert.isTrue(this.right == null, "The right input is already specified");
            this.right = Input.of(inputKey);
            return this;
        }
        
        public InterlinkStepBuilder right(int inputKey, String partKey)
        {
            Assert.isTrue(this.right == null, "The right input is already specified");
            this.right = Input.of(inputKey, partKey);
            return this;
        }
        
        public InterlinkStepBuilder configuration(InterlinkConfiguration<? extends InterlinkTool> configuration)
        {
            this.stepBuilder.configuration(configuration);
            return this;
        }
        
        public InterlinkStepBuilder outputKey(int outputKey)
        {
            this.stepBuilder.outputKey(outputKey);
            return this;
        }
        
        public InterlinkStepBuilder outputFormat(EnumDataFormat format)
        {
            this.stepBuilder.outputFormat(format);
            return this;
        }
        
        @Override
        protected Step build()
        {
            Assert.state(this.left != null, "The left input is not specified");
            Assert.state(this.right != null, "The right input is not specified");
            this.stepBuilder.input(left);
            this.stepBuilder.input(right);
            
            return this.stepBuilder.build();
        }
    }
    
    public class FuseStepBuilder extends StepBuilder
    {
        private final GenericStepBuilder stepBuilder;
        
        private Input left;
        
        private Input right;
        
        private Input link;
        
        public FuseStepBuilder(int key, String name)
        {
            this.stepBuilder = 
                ProcessDefinitionBuilder.this.new GenericStepBuilder(key, name, Step::new);
            this.stepBuilder.operation(EnumOperation.FUSION);
            this.stepBuilder.tool(EnumTool.FAGI);
        }
        
        public FuseStepBuilder group(int groupNumber)
        {
            this.stepBuilder.group(groupNumber);
            return this;
        }
        
        public FuseStepBuilder nodeName(String nodeName)
        {
            this.stepBuilder.nodeName(nodeName);
            return this;
        }
       
        public FuseStepBuilder left(int inputKey)
        {
            Assert.isTrue(this.left == null, "The left input is already specified");
            this.left = Input.of(inputKey);
            return this;
        }
        
        public FuseStepBuilder left(int inputKey, String partKey)
        {
            Assert.isTrue(this.left == null, "The left input is already specified");
            this.left = Input.of(inputKey, partKey);
            return this;
        }
        
        public FuseStepBuilder right(int inputKey)
        {
            Assert.isTrue(this.right == null, "The right input is already specified");
            this.right = Input.of(inputKey);
            return this;
        }
        
        public FuseStepBuilder right(int inputKey, String partKey)
        {
            Assert.isTrue(this.right == null, "The right input is already specified");
            this.right = Input.of(inputKey, partKey);
            return this;
        }
        
        public FuseStepBuilder link(int inputKey)
        {
            Assert.isTrue(this.link == null, "The input of owl:sameAs links is already specified");
            this.link = Input.of(inputKey);
            return this;
        }
        
        public FuseStepBuilder link(int inputKey, String partKey)
        {
            Assert.isTrue(this.link == null, "The input of owl:sameAs links is already specified");
            this.link = Input.of(inputKey, partKey);
            return this;
        }
        
        public FuseStepBuilder configuration(FuseConfiguration<? extends FuseTool> configuration)
        {
            this.stepBuilder.configuration(configuration);
            return this;
        }
        
        public FuseStepBuilder outputKey(int outputKey)
        {
            this.stepBuilder.outputKey(outputKey);
            return this;
        }
        
        public FuseStepBuilder outputFormat(EnumDataFormat format)
        {
            this.stepBuilder.outputFormat(format);
            return this;
        }
        
        @Override
        protected Step build()
        {
            Assert.state(this.left != null, "The left input is not specified");
            Assert.state(this.right != null, "The right input is not specified");
            Assert.state(this.link != null, "The input of owl:sameAs links is not specified");
            this.stepBuilder.input(left);
            this.stepBuilder.input(right);
            this.stepBuilder.input(link);
            
            return this.stepBuilder.build();
        }
    }
    
    public class RegisterStepBuilder extends StepBuilder
    {
        private final GenericStepBuilder stepBuilder;
        
        private ResourceMetadataCreate metadata;
        
        private ResourceIdentifier resourceIdentifier;
        
        protected RegisterStepBuilder(int key, String name) 
        {
            this.stepBuilder = 
                ProcessDefinitionBuilder.this.new GenericStepBuilder(key, name, Step::new);
            this.stepBuilder.operation(EnumOperation.REGISTER);
            this.stepBuilder.tool(EnumTool.REGISTER);
        }
        
        public RegisterStepBuilder group(int groupNumber)
        {
            this.stepBuilder.group(groupNumber);
            return this;
        }
        
        public RegisterStepBuilder nodeName(String nodeName)
        {
            this.stepBuilder.nodeName(nodeName);
            return this;
        }
        
        public RegisterStepBuilder resource(int inputKey)
        {
            Assert.isTrue(this.stepBuilder.input.isEmpty(), "A resource key is already specified");
            this.stepBuilder.input(inputKey);
            return this;
        }
        
        public RegisterStepBuilder resource(int inputKey, String partKey)
        {
            Assert.isTrue(this.stepBuilder.input.isEmpty(), "A resource key is already specified");
            this.stepBuilder.input(inputKey, partKey);
            return this;
        }
        
        /**
         * Set metadata to accompany resource
         * @param metadata
         */
        public RegisterStepBuilder metadata(ResourceMetadataCreate metadata)
        {
            Assert.isTrue(metadata != null && !StringUtils.isEmpty(metadata.getName()), 
                "Expected non-empty metadata to accompany this resource");
            this.metadata = metadata;
            return this;
        }
        
        /**
         * Register this resource as a new revision of an existing resource
         * @param resourceIdentifier The identifier of a catalog resource
         */
        public RegisterStepBuilder revisionOf(ResourceIdentifier resourceIdentifier)
        {
            Assert.notNull(resourceIdentifier, "A resource identifier is required");
            Assert.isTrue(resourceIdentifier.getId() > 0, "The id of referenced resource is invalid");
            Assert.isTrue(resourceIdentifier.getVersion() < 0, "A version must not be specified");
            this.resourceIdentifier = resourceIdentifier;
            return this;
        }
        
        protected Step build()
        {
            Assert.state(metadata != null, "The resource metadata are required");
            Assert.state(!stepBuilder.input.isEmpty(), 
                "No resource key is specified (as input for the registration step)");
            RegisterToCatalogConfiguration configuration = 
                new RegisterToCatalogConfiguration(metadata, resourceIdentifier);
            this.stepBuilder.configuration(configuration);
            return this.stepBuilder.build();
        }
    }
    
    public class ImportStepBuilder extends StepBuilder
    {
        private final GenericStepBuilder stepBuilder;
        
        private final URL url;
        
        private EnumDataFormat dataFormat;
        
        protected ImportStepBuilder(int key, String name, URL url) 
        {
            this.url = url;
            this.stepBuilder = 
                ProcessDefinitionBuilder.this.new GenericStepBuilder(key, name, Step::new);
            this.stepBuilder.operation(EnumOperation.IMPORT_DATA);
            this.stepBuilder.tool(EnumTool.IMPORTER);
            this.stepBuilder.source(new UrlDataSource(url));
        }
        
        public ImportStepBuilder outputKey(int outputKey)
        {
            this.stepBuilder.outputKey(outputKey);
            return this;
        }
        
        public ImportStepBuilder outputFormat(EnumDataFormat format)
        {
            Assert.notNull(format, "A data format is required");
            this.dataFormat = format;
            this.stepBuilder.outputFormat(format);
            return this;
        }
        
        @Override
        protected Step build()
        {
            this.stepBuilder.configuration(new ImportDataConfiguration(this.url, this.dataFormat));
            return stepBuilder.build();
        }
    }
    
    private final ClonerService cloner;
    
    private final String name;

    private String description;
    
    private int stepKeySequence = 0;

    private List<ProcessInput> resources = new ArrayList<ProcessInput>();

    private List<Step> steps = new ArrayList<Step>();

    public ProcessDefinitionBuilder(String name, ClonerService cloner) 
    {
        Assert.isTrue(!StringUtils.isEmpty(name), "A non-empty name is required");
        this.cloner = cloner;
        this.name = name;
    }
    
    public ProcessDefinitionBuilder description(String description)
    {
        this.description = description;
        return this;
    }

    /**
     * Designate a catalog resource as an input available to this process
     * 
     * @param name A user-friendly name for this input resource
     * @param key A resource key (unique across this process) for this resource to
     *   be referenced as an input (from other processing steps)
     * @param resourceIdentifier The pair of (id,version) identifying this catalog resource
     * @param resourceType The resource type
     * @return
     */
    public ProcessDefinitionBuilder resource(
        String name, int key, ResourceIdentifier resourceIdentifier, EnumResourceType resourceType)
    {
        Assert.isTrue(!StringUtils.isEmpty(name), "Expected a non-empty name for this resource");
        Assert.notNull(resourceIdentifier, "A resource identifier of (id, version) is required");
        
        CatalogResource resource = 
            new CatalogResource(key, name, resourceType, resourceIdentifier); 
        this.resources.add(resource);
        return this;
    }

    public ProcessDefinitionBuilder resource(String name, int key, ResourceIdentifier resourceIdentifier)
    {
        return this.resource(name, key, resourceIdentifier, EnumResourceType.POI_DATA);
    }
    
    /**
     * Designate an external source, a URL, as a input available to this process. 
     * 
     * <p>Note: This will actually add a preliminary importing step to this process, since
     * the resource is not directly available to other steps. This will happen regardless of
     * the URL (i.e even for URLs pointing to local resources (as a <tt>file:///a/b/c</tt>))
     * 
     * @param name A user-friendly name for this input resource
     * @param key A resource key (unique across this process) for this resource to
     *   be referenced as an input (from other processing steps)
     * @param url The source URL
     * @param The data format for this resource
     * @return
     */
    public ProcessDefinitionBuilder resource(String name, int key, URL url, EnumDataFormat dataFormat)
    {
        Assert.isTrue(!StringUtils.isEmpty(name), "Expected a non-empty name for this resource");
        Assert.notNull(url, "A source URL is required");
        Assert.notNull(dataFormat, "A data format is required");
        final IntFunction<ImportStepBuilder> stepBuilder =
            k -> this.new ImportStepBuilder(k, String.format("Import: %s", name), url);
        return this.addStep(b -> b.outputKey(key).outputFormat(dataFormat), stepBuilder);
    }
    
    /**
     * Add a generic step to this process.
     * 
     * @param name The user-friendly name for this step
     * @param configurer A function to build this step
     * @return  this builder
     */
    public ProcessDefinitionBuilder step(String name, Consumer<GenericStepBuilder> configurer)
    {
        Assert.isTrue(!StringUtils.isEmpty(name), "Expected a non-empty name");
        Assert.notNull(configurer, "Expected a non-null configurer for a step");
        return this.addStep(configurer, k -> this.new GenericStepBuilder(k, name));
    }
    
    /**
     * Add a transformation step on a resource or a external datasource. Commonly, this kind of step 
     * imports an external (to the application) data source into this process.
     *
     * <p>Currently, this step is always performed by Triplegeo tool, and is usually needed to 
     * make an external resource available to a process (and optionally to the resource catalog).
     *
     * @param name The user-friendly name for this step
     * @param configurer A function to build this step
     * @return this builder
     */
    public ProcessDefinitionBuilder transform(String name, Consumer<TransformStepBuilder> configurer)
    {
        Assert.isTrue(!StringUtils.isEmpty(name), "Expected a non-empty name");
        Assert.notNull(configurer, "Expected a non-null configurer for a step");
        return this.addStep(configurer, k -> this.new TransformStepBuilder(k, name));
    }
    
    /**
     * Add an interlinking step to this process.
     * 
     * @param name The user-friendly name for this step
     * @param configurer A function to build the step
     * @return this builder
     */
    public ProcessDefinitionBuilder interlink(String name, Consumer<InterlinkStepBuilder> configurer)
    {
        Assert.isTrue(!StringUtils.isEmpty(name), "Expected a non-empty name");
        Assert.notNull(configurer, "Expected a non-null configurer for a step");
        return this.addStep(configurer, k -> this.new InterlinkStepBuilder(k, name));
    }
    
    /**
     * Add a fusion step to this process.
     * 
     * @param name The user-friendly name for this step
     * @param configurer A function to build the step
     * @return this builder
     */
    public ProcessDefinitionBuilder fuse(String name, Consumer<FuseStepBuilder> configurer)
    {
        Assert.isTrue(!StringUtils.isEmpty(name), "Expected a non-empty name");
        Assert.notNull(configurer, "Expected a non-null configurer for a step");
        return this.addStep(configurer, k -> this.new FuseStepBuilder(k, name));
    }
    
    /**
     * Register an intermediate result (produced by a processing step) as a new catalog resource. 
     * 
     * @param name The user-friendly name for this step
     * @param configurer A function to build this step
     * @return this builder
     */
    public ProcessDefinitionBuilder register(String name, Consumer<RegisterStepBuilder> configurer)
    {
        Assert.isTrue(!StringUtils.isEmpty(name), "Expected a non-empty name");
        Assert.notNull(configurer, "Expected a non-null configurer for a step");
        return this.addStep(configurer, key -> new RegisterStepBuilder(key, name));
    }
    
    /**
     * Register an intermediate result (produced by a processing step) as a new catalog resource.
     * 
     * @param name The user-friendly name for this step
     * @param resourceKey The resource key defined as the output by some processing step 
     * @param partKey A named part of our resource
     * @param metadata The metadata to accompany the resource
     * @return this builder
     */
    public ProcessDefinitionBuilder register(
        String name, int resourceKey, String partKey, ResourceMetadataCreate metadata)
    {
        Assert.notNull(metadata, "The resource metadata are required");
        return this.register(name, b -> b.resource(resourceKey, partKey).metadata(metadata));
    }
    
    /**
     * Register an intermediate result (produced by a processing step) as a new catalog resource.
     * @see ProcessDefinitionBuilder#register(String, int, String, ResourceMetadataCreate)
     */
    public ProcessDefinitionBuilder register(
        String name, int resourceKey, ResourceMetadataCreate metadata)
    {
        Assert.notNull(metadata, "The resource metadata are required");
        return this.register(name, b -> b.resource(resourceKey).metadata(metadata));
    }
    
    /**
     * Register an intermediate result (produced by a processing step) as a new revision of an 
     * existing catalog resource.
     * 
     * @param name The user-friendly name for this step
     * @param resourceKey The resource key defined as the output by some processing step 
     * @param metadata The metadata to accompany the resource
     * @param target The identifier of an existing catalog resource
     * @return this builder
     */
    public ProcessDefinitionBuilder registerAsNewRevision(
        String name, int resourceKey, String partKey, ResourceMetadataCreate metadata, ResourceIdentifier target)
    {
        Assert.notNull(metadata, "The resource metadata are required");
        Assert.notNull(target, "A target resource is required");
        return this.register(name, b -> b.resource(resourceKey, partKey).metadata(metadata).revisionOf(target));
    }
    
    /**
     * Register an intermediate result (produced by a processing step) as a new revision of an 
     * existing catalog resource.
     * @see ProcessDefinitionBuilder#registerAsNewRevision(String, int, String, ResourceMetadataCreate, ResourceIdentifier)
     */
    public ProcessDefinitionBuilder registerAsNewRevision(
        String name, int resourceKey, ResourceMetadataCreate metadata, ResourceIdentifier target)
    {
        Assert.notNull(metadata, "The resource metadata are required");
        Assert.notNull(target, "A target resource is required");
        return this.register(name, b -> b.resource(resourceKey).metadata(metadata).revisionOf(target));
    }
    
    public ProcessDefinition build()
    {
        Assert.state(!StringUtils.isEmpty(this.name), "The name cannot be empty");

        final Set<String> names = this.steps.stream()
            .collect(Collectors.mapping(Step::name, Collectors.toSet()));
        
        final Set<String> nodeNames = this.steps.stream()
            .collect(Collectors.mapping(Step::nodeName, Collectors.toSet()));
        
        final Set<Integer> resourceKeys = this.resources.stream()
            .collect(Collectors.mapping(r -> r.key(), Collectors.toSet()));
        
        final Set<Integer> outputKeys = this.resources.stream()
            .filter(ProcessOutput.class::isInstance)
            .collect(Collectors.mapping(r -> r.key(), Collectors.toSet()));
        
        final Map<Integer, Step> stepByKey = this.steps.stream()
            .collect(Collectors.toMap(s -> s.key(), Function.identity()));
        
        final Map<Integer, Step> outputKeyToStep = this.resources.stream()
            .filter(ProcessOutput.class::isInstance)
            .map(ProcessOutput.class::cast)
            .collect(Collectors.toMap(r -> r.key(), r -> stepByKey.get(r.stepKey())));
        
        // Validate definition

        Assert.state(names.size() == this.steps.size(),
            "The list of given steps contains duplicate names!");
        
        Assert.state(nodeNames.size() == this.steps.size(),
            "The list of given steps contains duplicate names!");
        
        Assert.state(resourceKeys.size() == this.resources.size(),
            "The list of given resources contains duplicate keys!");
        
        Assert.state(resourceKeys.stream().allMatch(key -> key > 0), 
            "The resource keys must all be positive integers");
        
        Assert.state(this.steps.stream()
                .allMatch(step -> resourceKeys.containsAll(step.inputKeys())),
            "The input keys (for every step) must refer to existing resource keys");

        Assert.state(this.steps.stream()
                .filter(step -> step.operation() == EnumOperation.REGISTER)
                .allMatch(step -> outputKeys.containsAll(step.inputKeys())), 
           "The input key for a registration step must refer to an output of another step");
        
        final Set<Input> partialInputs = this.steps.stream()
            .flatMap(s -> s.input().stream())
            .filter(p -> p.partKey() != null)
            .collect(Collectors.toSet());
        
        Assert.state(partialInputs.stream().allMatch(p -> outputKeys.contains(p.inputKey())),
            "A partial input may only refer to output of another step");
        
        BiPredicate<Step, String> isPartOfOutput = (Step producer, String partKey) -> 
            producer.outputParts().stream()
                .anyMatch(p -> p.key().equals(partKey) && p.outputType() == EnumOutputType.OUTPUT);
        Assert.state(partialInputs.stream()
                .allMatch(p -> isPartOfOutput.test(outputKeyToStep.get(p.inputKey()), p.partKey())),
            "A partial input refers to a non-existing part of output of another step");

        // The definition seems valid

        ProcessDefinition definition = new ProcessDefinition(this.name, this.resources, this.steps);
        definition.setDescription(this.description);
        
        return definition;
    }

    private <B extends StepBuilder> ProcessDefinitionBuilder addStep(
        Consumer<B> configurer, IntFunction<B> builderFactory)
    {
        Assert.state(builderFactory != null, "Expected a non-null factory for a StepBuilder");
        
        int stepKey = this.stepKeySequence++;
        
        // Build step
        
        B stepBuilder = builderFactory.apply(stepKey);
        configurer.accept(stepBuilder);
        Step step = stepBuilder.build();
        
        // Perform post-construct initialization
        
        step.initialize();
        
        // Add to this process
        
        this.steps.add(step);
        
        if (step.outputKey != null)
            this.resources.add(ProcessOutput.fromStep(step));
        
        return this;
    }
}
