package eu.slipo.workbench.common.model.process;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import eu.slipo.workbench.common.model.poi.EnumDataFormat;
import eu.slipo.workbench.common.model.poi.EnumOperation;
import eu.slipo.workbench.common.model.poi.EnumResourceType;
import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.resource.DataSource;
import eu.slipo.workbench.common.model.resource.ResourceMetadataCreate;
import eu.slipo.workbench.common.model.tool.MetadataRegistrationConfiguration;
import eu.slipo.workbench.common.model.tool.ToolConfiguration;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;

/**
 * A builder for a {@link ProcessDefinition}
 */
public class ProcessDefinitionBuilder {

    private String name;

    private int stepKey = 0;

    private List<ProcessInput> resources = new ArrayList<ProcessInput>();

    private List<Step> steps = new ArrayList<Step>();

    public ProcessDefinitionBuilder() {}

    public static ProcessDefinitionBuilder create()
    {
        return new ProcessDefinitionBuilder();
    }
    
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
     * Designate a catalog resource as an input of this process
     * @param resource A catalog resource
     */
    public ProcessDefinitionBuilder resource(CatalogResource resource)
    {
        this.resources.add(resource);
        return this;
    }

    private ProcessDefinitionBuilder step(Step step) {
        this.steps.add(step);
        return this;
    }

    /**
     * Add a step to this process. This step expects input from either catalog resources
     * or from the output of other steps.
     *
     * @param group
     * @param name
     * @param tool
     * @param operation
     * @param configuration A tool-specific configuration bean
     * @param resourceKeys The resource keys for the input of this step
     * @param outputKey The resource key of the (single) output produced by this step
     * @return
     */
    public ProcessDefinitionBuilder step(
        int group,
        String name,
        EnumTool tool,
        EnumOperation operation,
        ToolConfiguration configuration,
        List<Integer> resourceKeys,
        int outputKey,
        EnumDataFormat outputFormat)
    {
        int key = ++this.stepKey;

        Step step = Step.builder(key, group, name)
            .tool(tool)
            .configuration(configuration)
            .operation(operation)
            .input(resourceKeys)
            .outputKey(outputKey)
            .outputFormat(outputFormat)
            .build();

        EnumResourceType resourceType = tool == EnumTool.LIMES?
            EnumResourceType.POI_LINKED_DATA : EnumResourceType.POI_DATA;
        this.resources.add(new ProcessOutput(outputKey, resourceType, name, key, tool));

        return this.step(step);
    }

    /**
     * Add a special-purpose transformation step that imports an external (to the application)
     * data source into this process.
     *
     * <p>This step is always performed by Triplegeo tool, and is usually needed to make an
     * external resource available to a process (and to the resource catalog).
     *
     * @param group
     * @param name
     * @param source
     * @param configuration
     * @param outputKey
     * @return
     */
    public ProcessDefinitionBuilder transform(
        int group,
        String name,
        DataSource source,
        TriplegeoConfiguration configuration,
        int outputKey)
    {
        int key = ++this.stepKey;

        Step step = Step.builder(key, group, name)
            .tool(EnumTool.TRIPLEGEO)
            .configuration(configuration)
            .operation(EnumOperation.TRANSFORM)
            .source(source)
            .outputKey(outputKey)
            .outputFormat(configuration.getOutputFormat())
            .build();

        EnumResourceType resourceType = EnumResourceType.POI_DATA;
        this.resources.add(new ProcessOutput(outputKey, resourceType, name, key, EnumTool.TRIPLEGEO));

        return this.step(step);
    }

    /**
     * Register an intermediate result (expected to be produced by this process) to the catalog.
     * The actual registration will take place after successful completion of the enclosing
     * process.
     *
     * @param group
     * @param name A user-friendly name for this step
     * @param metadata The metadata that should accompany the registered resource
     * @param resourceKey The key that identifies the resource to register
     * @return
     */
    public ProcessDefinitionBuilder register(
        int group,
        String name,
        ResourceMetadataCreate metadata,
        Integer resourceKey)
    {
        int key = ++this.stepKey;

        Step step = Step.builder(key, group, name)
            .tool(EnumTool.REGISTER_METADATA)
            .configuration(new MetadataRegistrationConfiguration(metadata))
            .operation(EnumOperation.REGISTER)
            .input(resourceKey)
            .build();

        return this.step(step);
    }

    public ProcessDefinitionBuilder reset()
    {
        this.name = null;
        this.stepKey = 0;
        this.resources.clear();
        this.steps.clear();
        return this;
    }

    public ProcessDefinition build()
    {
        Assert.state(!StringUtils.isEmpty(this.name), "The name cannot be empty");

        // Validate definition

        final List<Integer> resourceKeys = this.resources.stream()
            .map(ProcessInput::getKey)
            .distinct()
            .collect(Collectors.toList());

        Assert.state(
            resourceKeys.size() == this.resources.size(),
            "The list of given resources contains duplicate keys!");

        Assert.state(
            this.steps.stream().allMatch(step -> resourceKeys.containsAll(step.inputKeys())),
            "The input keys (for every step) must refer to existing resource keys");

        // The definition seems valid

        return new ProcessDefinition(this.name, this.resources, this.steps);
    }
}
