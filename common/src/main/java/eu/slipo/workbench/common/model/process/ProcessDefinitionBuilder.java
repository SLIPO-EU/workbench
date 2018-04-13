package eu.slipo.workbench.common.model.process;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumOperation;
import eu.slipo.workbench.common.model.poi.EnumResourceType;
import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.resource.DataSource;
import eu.slipo.workbench.common.model.resource.ResourceIdentifier;
import eu.slipo.workbench.common.model.resource.ResourceMetadataCreate;
import eu.slipo.workbench.common.model.tool.InterlinkConfiguration;
import eu.slipo.workbench.common.model.tool.MetadataRegistrationConfiguration;
import eu.slipo.workbench.common.model.tool.ToolConfiguration;
import eu.slipo.workbench.common.model.tool.TransformConfiguration;

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
    public static class BasicStepBuilder extends StepBuilder
    {
        private final Supplier<Step> stepFactory;
        
        protected final int key;

        protected final String name;

        protected String nodeName;
        
        protected int group = 0;
        
        protected EnumOperation operation;

        protected EnumTool tool;

        protected List<Integer> inputKeys = new ArrayList<>();

        protected List<DataSource> sources = new ArrayList<>();

        protected Integer outputKey;
        
        protected EnumDataFormat outputFormat;

        protected ToolConfiguration configuration;

        /**
         * Create a builder for a step ({@link Step}).
         *
         * @param key A unique (across its process) key for this step
         * @param name A name (preferably unique) for this step.
         * @param stepFactory A factory to create instances of {@link Step}
         */
        protected BasicStepBuilder(int key, String name, Supplier<Step> stepFactory)
        {
            Assert.isTrue(!StringUtils.isEmpty(name), "Expected a name for this step");
            Assert.notNull(stepFactory, "A step factory is required");
            this.key = key;
            this.name = name;
            this.nodeName = Step.slugifyName(name);
            this.stepFactory = stepFactory;
        }

        protected BasicStepBuilder(int key, String name)
        {
            this(key, name, Step::new);
        }
        
        public BasicStepBuilder nodeName(String nodeName)
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
        public BasicStepBuilder operation(EnumOperation operation)
        {
            Assert.notNull(operation, "Expected an non-null operation");
            this.operation = operation;
            return this;
        }

        public BasicStepBuilder group(int groupNumber)
        {
            this.group = groupNumber;
            return this;
        }
        
        /**
         * Set the tool that implements the step operation
         * @param tool
         */
        public BasicStepBuilder tool(EnumTool tool)
        {
            Assert.notNull(tool, "Expected a tool constant");
            this.tool = tool;
            return this;
        }

        /**
         * Provide the tool-specific configuration
         * @param configuration A tool-specific configuration bean
         */
        public BasicStepBuilder configuration(ToolConfiguration configuration)
        {
            Assert.notNull(configuration, "Expected a non-null configuration");
            
            try {
                // Make a defensive copy of the configuration bean
                // Todo Maybe clone using serialize + deserialize operations?
                this.configuration = (ToolConfiguration) BeanUtils.cloneBean(configuration);
            } catch (ReflectiveOperationException ex) {
                throw new IllegalStateException("Cannot clone the configuration bean", ex);
            }
            
            return this;
        }

        /**
         * Assign a unique key to the resource generated as output of this step inside
         * a process.
         * @param outputKey
         */
        public BasicStepBuilder outputKey(int outputKey)
        {
            this.outputKey = outputKey;
            return this;
        }

        /**
         * Set the keys of process-wide resources that should be input to this step
         * @param inputKeys A list of resource keys
         */
        public BasicStepBuilder input(List<Integer> inputKeys)
        {
            this.inputKeys.addAll(inputKeys);
            return this;
        }

        /**
         * @see {@link Step.BasicStepBuilder#input(List)}
         * @param inputKey
         */
        public BasicStepBuilder input(int inputKey)
        {
            this.inputKeys.add(inputKey);
            return this;
        }

        /**
         * @see {@link Step.BasicStepBuilder#input(List)}
         * @param inputKey
         */
        public BasicStepBuilder input(int inputKey1, int inputKey2)
        {
            this.inputKeys.add(inputKey1);
            this.inputKeys.add(inputKey2);
            return this;
        }
        
        /**
         * @see {@link Step.BasicStepBuilder#input(List)}
         * @param inputKey
         */
        public BasicStepBuilder input(int ...inputKeys)
        {
            this.inputKeys.addAll(Arrays.asList(ArrayUtils.toObject(inputKeys)));
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
        public BasicStepBuilder source(List<DataSource> s)
        {
            this.sources.addAll(s);
            return this;
        }

        /**
         * @see {@link Step.BasicStepBuilder#source(List)}
         * @param s
         */
        public BasicStepBuilder source(DataSource s)
        {
            Assert.notNull(s, "Expected a non-null data source");
            this.sources.add(s);
            return this;
        }
        
        /**
         * @see {@link Step.BasicStepBuilder#source(List)}
         * @param s
         */
        public BasicStepBuilder source(DataSource s1, DataSource s2)
        {
            Assert.notNull(s1, "Expected a non-null data source");
            Assert.notNull(s2, "Expected a non-null data source");
            this.sources.add(s1);
            this.sources.add(s2);
            return this;
        }
        
        public BasicStepBuilder outputFormat(EnumDataFormat outputFormat)
        {
            this.outputFormat = outputFormat;
            return this;
        }

        protected Step build()
        {
            Assert.state(this.operation != null, "The operation must be specified");
            Assert.state(this.tool != null, "The tool must be specified");
            Assert.state(this.configuration != null, "The tool configuration must be provided");
            Assert.state(operation == EnumOperation.REGISTER || 
                    (this.outputKey != null && this.outputFormat != null),
                "An output key and format is required for a non-registration step");
            Assert.state(this.tool.supportsOperation(operation), 
                "The operation is not supported by given tool");
            Assert.state(!this.inputKeys.isEmpty() || !this.sources.isEmpty(),
                "The list of data sources and list of input keys cannot be both empty!");

            Step step = this.stepFactory.get();

            step.key = this.key;
            step.group = this.group;
            step.name = this.name;
            step.nodeName = this.nodeName;
            step.operation = this.operation;
            step.tool = this.tool;
            step.sources = new ArrayList<>(this.sources);
            step.inputKeys = new ArrayList<>(this.inputKeys);
            step.outputKey = this.outputKey;
            step.outputFormat = this.outputFormat;
            step.configuration = this.configuration;
           
            return step;
        }
    }
    
    public static class TransformStepBuilder extends StepBuilder
    {
        private final BasicStepBuilder stepBuilder;
        
        protected TransformStepBuilder(int key, String name) 
        {
            this.stepBuilder = new BasicStepBuilder(key, name, TransformStep::new)
                .operation(EnumOperation.TRANSFORM)
                .tool(EnumTool.TRIPLEGEO);
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
            Assert.isTrue(
                this.stepBuilder.sources.isEmpty() && this.stepBuilder.inputKeys.isEmpty(), 
                "A input (either datasource or a key) is already specified for this step");
            this.stepBuilder.source(source);
            return this;
        }
        
        public TransformStepBuilder input(int inputKey)
        {
            Assert.isTrue(
                this.stepBuilder.sources.isEmpty() && this.stepBuilder.inputKeys.isEmpty(), 
                "A input (either datasource or a key) is already specified for this step");
            this.stepBuilder.input(inputKey);
            return this;
        }
        
        public TransformStepBuilder configuration(TransformConfiguration configuration)
        {
            Assert.notNull(configuration, "Expected a non-null configuration");
            final EnumDataFormat format = this.stepBuilder.outputFormat; 
            Assert.isTrue(this.stepBuilder.outputFormat == null || configuration.getOutputFormat() == null || 
                (format == configuration.getOutputFormat()), 
                "The output format must agree with the one supplied at step configuration");
            
            this.stepBuilder.configuration(configuration);
            this.stepBuilder.tool(configuration.getTool());
            this.stepBuilder.outputFormat(configuration.getOutputFormat());
            return this;
        }
        
        public TransformStepBuilder outputKey(int outputKey)
        {
            this.stepBuilder.outputKey(outputKey);
            return this;
        }
        
        public TransformStepBuilder outputFormat(EnumDataFormat format)
        {
            final TransformConfiguration configuration = 
                (TransformConfiguration) this.stepBuilder.configuration;
            Assert.isTrue(configuration == null || format == configuration.getOutputFormat(), 
                "The output format is different from the one supplied to configuration");
            this.stepBuilder.outputFormat(format);
            return this;
        }
        
        @Override
        protected Step build()
        {
            return this.stepBuilder.build();
        }
    }
    
    public static class InterlinkStepBuilder extends StepBuilder
    {
        private final BasicStepBuilder stepBuilder;

        public InterlinkStepBuilder(int key, String name)
        {
            this.stepBuilder = new BasicStepBuilder(key, name, InterlinkStep::new)
                .operation(EnumOperation.INTERLINK)
                .tool(EnumTool.LIMES);
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
        
        /**
         * Link 2 data sources
         * 
         * @param first A data source
         * @param second Another data source
         */
        public InterlinkStepBuilder link(DataSource first, DataSource second)
        {
            Assert.isTrue(
                this.stepBuilder.sources.isEmpty() && this.stepBuilder.inputKeys.isEmpty(), 
                "An input pair (either as datasources or as keys) is already specified for this step");
            this.stepBuilder.source(first, second);
            return this;
        }
        
        /**
         * Link 2 inputs (i.e catalog resources or intermediate processing results)
         * 
         * @param first The key of the first input
         * @param second The key of the second input 
         * @return
         */
        public InterlinkStepBuilder link(int first, int second)
        {
            Assert.isTrue(
                this.stepBuilder.sources.isEmpty() && this.stepBuilder.inputKeys.isEmpty(), 
                "An input pair (either as datasources or as keys) is already specified for this step");
            this.stepBuilder.input(first, second);
            return this;
        }
        
        public InterlinkStepBuilder configuration(InterlinkConfiguration configuration)
        {
            Assert.notNull(configuration, "Expected a non-null configuration");
            final EnumDataFormat format = this.stepBuilder.outputFormat; 
            Assert.isTrue(format == null || configuration.getOutputFormat() == null || 
                (format == configuration.getOutputFormat()), 
                "The output format must agree with the one supplied at step configuration");
            
            this.stepBuilder.configuration(configuration);
            this.stepBuilder.tool(configuration.getTool());
            this.stepBuilder.outputFormat(configuration.getOutputFormat());
            return this;
        }
        
        public InterlinkStepBuilder outputKey(int outputKey)
        {
            this.stepBuilder.outputKey(outputKey);
            return this;
        }
        
        public InterlinkStepBuilder outputFormat(EnumDataFormat format)
        {
            final InterlinkConfiguration configuration = 
                (InterlinkConfiguration) this.stepBuilder.configuration;
            Assert.isTrue(configuration == null || format == configuration.getOutputFormat(), 
                "The output format is different from the one supplied to configuration");
            this.stepBuilder.outputFormat(format);
            return this;
        }
        
        @Override
        protected Step build()
        {
            return this.stepBuilder.build();
        }
    }
    
    public static class RegisterStepBuilder extends StepBuilder
    {
        private final BasicStepBuilder stepBuilder;
        
        private ResourceMetadataCreate metadata;
        
        private ResourceIdentifier resourceIdentifier;
        
        protected RegisterStepBuilder(int key, String name) 
        {
            this.stepBuilder = new BasicStepBuilder(key, name, RegisterStep::new)
                .operation(EnumOperation.REGISTER)
                .tool(EnumTool.REGISTER);
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
        
        public RegisterStepBuilder resource(int resourceKey)
        {
            Assert.isTrue(this.stepBuilder.inputKeys.isEmpty(), 
                "A resource key is already specified");
            this.stepBuilder.input(resourceKey);
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
            Assert.state(metadata != null, 
                "The required metadata for a resource are missing");
            Assert.state(!stepBuilder.inputKeys.isEmpty(), 
                "No resource key is specified (as input for the registration step)");
            MetadataRegistrationConfiguration configuration = 
                new MetadataRegistrationConfiguration(metadata, resourceIdentifier);
            this.stepBuilder.configuration(configuration);
            return this.stepBuilder.build();
        }
    }
    
    private String name;

    private String description;
    
    private int stepKeySequence = 0;

    private List<ProcessInput> resources = new ArrayList<ProcessInput>();

    private List<Step> steps = new ArrayList<Step>();

    protected ProcessDefinitionBuilder() {}
    
    public static ProcessDefinitionBuilder create(String name) 
    {
        return (new ProcessDefinitionBuilder()).name(name);
    }

    public ProcessDefinitionBuilder name(String name)
    {
        Assert.isTrue(!StringUtils.isEmpty(name), "A non-empty name is required");
        this.name = name;
        return this;
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
     * @param inputKey A resource key (unique across this process) for this resource to
     *   be referenced as an input (from other processing steps)
     * @param resourceIdentifier The pair of (id,version) identifying this catalog resource
     * @param resourceType The resource type
     * @return
     */
    public ProcessDefinitionBuilder resource(
        String name, int inputKey, ResourceIdentifier resourceIdentifier, EnumResourceType resourceType)
    {
        Assert.isTrue(!StringUtils.isEmpty(name), 
            "Expected a non-empty name for this resource");
        Assert.notNull(resourceIdentifier, 
            "A resource identifier (pair of id and version) is required");
        this.resources.add(
            new CatalogResource(inputKey, name, resourceType, resourceIdentifier));
        return this;
    }

    public ProcessDefinitionBuilder resource(String name, int inputKey, ResourceIdentifier resourceIdentifier)
    {
        return this.resource(name, inputKey, resourceIdentifier, EnumResourceType.POI_DATA);
    }
    
    /**
     * Add a step to this process. This step expects input from either catalog resources
     * or from the output of other steps.
     * 
     * @param name The user-friendly name for this step
     * @param configurer A function to build this step
     * @return  this builder
     */
    public ProcessDefinitionBuilder step(String name, Consumer<BasicStepBuilder> configurer)
    {
        Assert.isTrue(!StringUtils.isEmpty(name), "Expected a non-empty name");
        Assert.notNull(configurer, "Expected a non-null configurer for a step");
        return this.addStep(name, configurer, BasicStepBuilder::new);
    }
    
    /**
     * Add a special-purpose transformation step that imports an external (to the application)
     * data source into this process.
     *
     * <p>Currently, this step is always performed by Triplegeo tool, and is usually needed to 
     * make an external resource available to a process (and optionally to the resource catalog).
     *
     * @param name The user-friendly name for this step
     * @param configurer A function to build this step
     * @return  this builder
     */
    public ProcessDefinitionBuilder transform(String name, Consumer<TransformStepBuilder> configurer)
    {
        Assert.isTrue(!StringUtils.isEmpty(name), "Expected a non-empty name");
        Assert.notNull(configurer, "Expected a non-null configurer for a step");
        return this.addStep(name, configurer, TransformStepBuilder::new);
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
        return this.addStep(name, configurer, RegisterStepBuilder::new);
    }
    
    /**
     * Register an intermediate result (produced by a processing step) as a new catalog resource.
     * 
     * @param name The user-friendly name for this step
     * @param resourceKey The resource key defined as the output by some processing step 
     * @param metadata The metadata to accompany the resource
     * @return this builder
     */
    public ProcessDefinitionBuilder register(String name, int resourceKey, ResourceMetadataCreate metadata)
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
        String name, int resourceKey, ResourceMetadataCreate metadata, ResourceIdentifier target)
    {
        Assert.notNull(metadata, "The resource metadata are required");
        Assert.notNull(target, "A target resource is required");
        return this.register(
            name, b -> b.resource(resourceKey).metadata(metadata).revisionOf(target));
    }
    
    public ProcessDefinition build()
    {
        Assert.state(!StringUtils.isEmpty(this.name), "The name cannot be empty");

        // Validate definition

        final Set<String> names = this.steps.stream()
            .collect(Collectors.mapping(Step::name, Collectors.toSet()));
        Assert.state(names.size() == this.steps.size(),
            "The list of given steps contains duplicate names!");
        
        final Set<String> nodeNames = this.steps.stream()
            .collect(Collectors.mapping(Step::nodeName, Collectors.toSet()));
        Assert.state(nodeNames.size() == this.steps.size(),
            "The list of given steps contains duplicate names!");
        
        final Set<Integer> resourceKeys = this.resources.stream()
            .collect(Collectors.mapping(r -> r.key(), Collectors.toSet()));
        
        final Set<Integer> outputKeys = this.resources.stream()
            .filter(r -> r instanceof ProcessOutput)
            .collect(Collectors.mapping(r -> r.key(), Collectors.toSet()));
        
        Assert.state(resourceKeys.size() == this.resources.size(),
            "The list of given resources contains duplicate keys!");
        
        Assert.state(resourceKeys.stream().allMatch(key -> key > 0), 
            "The resource keys must all be positive integers");
        
        Assert.state(
            this.steps.stream()
                .allMatch(step -> resourceKeys.containsAll(step.inputKeys())),
            "The input keys (for every step) must refer to existing resource keys");

        Assert.state(
            this.steps.stream()
                .filter(step -> step.operation == EnumOperation.REGISTER)
                .allMatch(step -> outputKeys.containsAll(step.inputKeys())), 
           "The input key for a registration step must refer to an output of another step");
        
        // The definition seems valid

        ProcessDefinition definition = new ProcessDefinition(this.name, this.resources, this.steps);
        definition.setDescription(this.description);
        
        return definition;
    }
    
    private <B extends StepBuilder> ProcessDefinitionBuilder addStep(
        String name, Consumer<B> configurer, BiFunction<Integer, String, B> builderFactory)
    {
        Assert.state(builderFactory != null, "Expected a non-null factory for a StepBuilder");
        
        int stepKey = this.stepKeySequence++;
        
        B stepBuilder = builderFactory.apply(stepKey, name);
        configurer.accept(stepBuilder);
        Step step = stepBuilder.build();

        this.steps.add(step);
        
        if (step.outputKey != null)
            this.resources.add(ProcessOutput.fromStep(step));
        
        return this;
    }
}
