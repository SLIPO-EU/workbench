package eu.slipo.workbench.common.model.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumOperation;
import eu.slipo.workbench.common.model.poi.EnumResourceType;
import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.resource.DataSource;
import eu.slipo.workbench.common.model.resource.ResourceIdentifier;
import eu.slipo.workbench.common.model.resource.ResourceMetadataCreate;
import eu.slipo.workbench.common.model.tool.MetadataRegistrationConfiguration;
import eu.slipo.workbench.common.model.tool.ToolConfiguration;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;

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
            this.stepFactory = stepFactory;
        }

        protected BasicStepBuilder(int key, String name)
        {
            this(key, name, Step::new);
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
            this.configuration = configuration;
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
            Assert.state(!this.inputKeys.isEmpty() || !this.sources.isEmpty(),
                "The list of data sources and list of input keys cannot be both empty!");

            Step step = this.stepFactory.get();

            step.key = this.key;
            step.group = this.group;
            step.name = this.name;
            step.operation = this.operation;
            step.tool = this.tool;
            step.sources = new ArrayList<>(this.sources);
            step.inputKeys = new ArrayList<>(this.inputKeys);
            step.outputKey = this.outputKey;
            step.outputFormat = this.outputFormat;

            try {
                // Make a defensive copy of the configuration bean
                step.configuration = (ToolConfiguration) BeanUtils.cloneBean(configuration);
            } catch (ReflectiveOperationException ex) {
                throw new IllegalStateException("Cannot clone configuration", ex);
            }

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
        
        public TransformStepBuilder source(DataSource s)
        {
            Assert.isTrue(this.stepBuilder.sources.isEmpty(), 
                "A data source is already specified for this step");
            this.stepBuilder.source(s);
            return this;
        }
        
        public TransformStepBuilder configuration(TriplegeoConfiguration configuration)
        {
            Assert.notNull(configuration, "Expected a non-null configuration");
            
            final EnumDataFormat format = this.stepBuilder.outputFormat; 
            Assert.isTrue(format == null || configuration.getOutputFormat() == null || 
                (format == configuration.getOutputFormat()), 
                "The output format must agree with the one supplied at step configuration");
            
            this.stepBuilder.configuration(configuration);
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
            final TriplegeoConfiguration configuration = 
                (TriplegeoConfiguration) this.stepBuilder.configuration;
            Assert.isTrue(configuration == null || format == configuration.getOutputFormat(), 
                "The output format is different from the one supplied to triplegeo configuration");
            this.stepBuilder.outputFormat(format);
            return this;
        }
        
        protected Step build()
        {
            Assert.state(this.stepBuilder.inputKeys.isEmpty(), 
                "Did not expect any input keys (only external data sources)");
            Assert.state(this.stepBuilder.sources.size() == 1, 
                "Expected a single data source to import and transform data");
            return this.stepBuilder.build();
        }
    }
    
    public static class RegisterStepBuilder extends StepBuilder
    {
        private final BasicStepBuilder stepBuilder;
        
        private ResourceMetadataCreate metadata;
        
        protected RegisterStepBuilder(int key, String name) 
        {
            this.stepBuilder = new BasicStepBuilder(key, name, RegisterStep::new)
                .operation(EnumOperation.REGISTER)
                .tool(EnumTool.REGISTER_METADATA);
        }
        
        public RegisterStepBuilder resource(int resourceKey)
        {
            Assert.isTrue(this.stepBuilder.inputKeys.isEmpty(), 
                "A resource key is already specified");
            this.stepBuilder.input(resourceKey);
            return this;
        }
        
        public RegisterStepBuilder metadata(ResourceMetadataCreate metadata)
        {
            Assert.isTrue(metadata != null && !StringUtils.isEmpty(metadata.getName()), 
                "Expected non-empty metadata to accompany this resource");
            final MetadataRegistrationConfiguration configuration = 
                new MetadataRegistrationConfiguration(metadata);
            this.stepBuilder.configuration(configuration);
            this.metadata = metadata;
            return this;
        }
        
        protected Step build()
        {
            Assert.state(metadata != null, 
                "The required metadata for a resource are missing");
            Assert.state(!stepBuilder.inputKeys.isEmpty(), 
                "No resource key is specified (as input for the registration step)");
            return this.stepBuilder.build();
        }
    }
    
    private String name;

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
     * Register an intermediate result (produced by this process) to the catalog. The actual
     * registration will take place after successful completion of the entire process.
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
    
    public ProcessDefinitionBuilder register(String name, int resourceKey, ResourceMetadataCreate metadata)
    {
        Assert.notNull(metadata, "The resource metadata are required");
        return this.register(name, b -> b.resource(resourceKey).metadata(metadata));
    }
    
    public ProcessDefinition build()
    {
        Assert.state(!StringUtils.isEmpty(this.name), "The name cannot be empty");

        // Validate definition

        final Set<String> names = this.steps.stream()
            .map(Step::name)
            .collect(Collectors.toSet());
        
        Assert.state(names.size() == this.steps.size(),
            "The list of given steps contains duplicate names!");
        
        final Set<Integer> resourceKeys = this.resources.stream()
            .map(ProcessInput::getKey)
            .collect(Collectors.toSet());

        Assert.state(resourceKeys.size() == this.resources.size(),
            "The list of given resources contains duplicate keys!");
        
        Assert.state(resourceKeys.stream().allMatch(key -> key > 0), 
            "The resource keys must all be positive integers");
        
        Assert.state(this.steps.stream()
                .allMatch(step -> resourceKeys.containsAll(step.inputKeys())),
            "The input keys (for every step) must refer to existing resource keys");

        // The definition seems valid

        return new ProcessDefinition(this.name, this.resources, this.steps);
    }
    
    private <B extends StepBuilder> ProcessDefinitionBuilder addStep(
        String name, Consumer<B> configurer, BiFunction<Integer, String, B> builderFactory)
    {
        Assert.state(builderFactory != null, "Expected a non-null factory for a StepBuilder");
        
        int stepKey = ++this.stepKeySequence;
        
        B stepBuilder = builderFactory.apply(stepKey, name);
        configurer.accept(stepBuilder);
        Step step = stepBuilder.build();

        this.steps.add(step);
        
        if (step.outputKey != null)
            this.resources.add(ProcessOutput.fromStep(step));
        
        return this;
    }
}
