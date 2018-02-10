package eu.slipo.workbench.common.model.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public static ProcessDefinitionBuilder create() {
        return new ProcessDefinitionBuilder();
    }

    public ProcessDefinitionBuilder name(String name) 
    {
        this.name = name;
        return this;
    }
    
    public ProcessDefinitionBuilder resource(ProcessInput resource) {
        this.resources.add(resource);
        return this;
    }

    private ProcessDefinitionBuilder step(Step step) {
        this.steps.add(step);
        return this;
    }

    public ProcessDefinitionBuilder step(
        int group, 
        String name, 
        EnumTool tool,
        EnumOperation operation, 
        ToolConfiguration configuration, 
        List<Integer> resources)
    {
        int key = ++this.stepKey;
        
        Step step = new Step(
            name, 
            key, 
            group, 
            tool, 
            operation, 
            configuration, 
            new ArrayList<>(resources), 
            null);

        return this.step(step);
    }

    public ProcessDefinitionBuilder step(
        int group, 
        String name, 
        EnumTool tool,
        EnumOperation operation, 
        ToolConfiguration configuration, 
        List<Integer> resources,
        int output)
    {
        int key = ++this.stepKey;
        
        Step step = new Step(
            name, 
            key, 
            group, 
            tool, 
            operation, 
            configuration, 
            new ArrayList<>(resources), 
            output);
        
        // Output
        EnumResourceType resourceType = tool == EnumTool.LIMES? 
            EnumResourceType.POI_LINKED_DATA : EnumResourceType.POI_DATA;
        this.resources.add(new ProcessOutput(output, resourceType, name, this.stepKey, tool));

        return this.step(step);
    }

    public ProcessDefinitionBuilder transform(
        int group, 
        String name, 
        DataSource dataSource,
        TriplegeoConfiguration configuration,
        int output)
    {
        int key = ++this.stepKey;
        
        Step step = new Step(
            name, 
            key, 
            group, 
            EnumTool.TRIPLEGEO,
            EnumOperation.TRANSFORM,
            configuration,
            Collections.emptyList(),
            output);
        
        // Output
        this.resources.add(
            new ProcessOutput(output, EnumResourceType.POI_DATA, name, this.stepKey, EnumTool.TRIPLEGEO));
        
        return this.step(step);
    }

    public ProcessDefinitionBuilder register(
        int group,
        String name,
        ResourceMetadataCreate metadata,
        Integer resource)
    {
        int key = ++this.stepKey;

        Step step = new Step(
            name, 
            key, 
            group, 
            EnumTool.CATALOG, 
            EnumOperation.REGISTER,
            new MetadataRegistrationConfiguration(metadata),
            Collections.singletonList(resource),
            null
        );

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
        return new ProcessDefinition(name, resources, steps);
    }
}
