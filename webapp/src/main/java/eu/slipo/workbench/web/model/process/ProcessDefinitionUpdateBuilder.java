package eu.slipo.workbench.web.model.process;

import java.util.ArrayList;
import java.util.List;

import eu.slipo.workbench.web.model.EnumOperation;
import eu.slipo.workbench.web.model.EnumResourceType;
import eu.slipo.workbench.web.model.EnumTool;
import eu.slipo.workbench.web.model.resource.DataSource;
import eu.slipo.workbench.web.model.resource.ResourceMetadataCreate;

/**
 * Helper builder for composing a {@link ProcessDefinitionUpdate} instance
 */
public class ProcessDefinitionUpdateBuilder {

    private int stepKey = 0;

    private List<ProcessInput> resources = new ArrayList<ProcessInput>();

    private List<Step> steps = new ArrayList<Step>();

    private ProcessDefinitionUpdateBuilder() {

    }

    public static ProcessDefinitionUpdateBuilder create() {
        return new ProcessDefinitionUpdateBuilder();
    }

    public ProcessDefinitionUpdateBuilder resource(ProcessInput resource) {
        this.resources.add(resource);
        return this;
    }

    private ProcessDefinitionUpdateBuilder step(Step step) {
        this.steps.add(step);

        return this;
    }

    public ProcessDefinitionUpdateBuilder step(
            int group,
            String name,
            EnumTool tool,
            EnumOperation operation,
            ToolConfiguration configuration,
            List<Integer> resources) {

        this.stepKey++;

        // Step
        Step step = new Step(name, this.stepKey, group, tool, operation, configuration);
        // Input
        for (Integer r : resources) {
            step.getConfiguration().getResources().add(r);
        }

        return this.step(step);
    }

    public ProcessDefinitionUpdateBuilder step(
            int group,
            String name,
            EnumTool tool,
            EnumOperation operation,
            ToolConfiguration configuration,
            List<Integer> resources,
            int output) {

        this.stepKey++;

        // Step
        Step step = new Step(name, this.stepKey, group, tool, operation, configuration, output);
        // Input
        for (Integer r : resources) {
            step.getConfiguration().getResources().add(r);
        }
        // Output
        EnumResourceType resourceType = (tool == EnumTool.LIMES ? EnumResourceType.POI_LINKED_DATA : EnumResourceType.POI_DATA);
        this.resources.add(new ProcessOutput(output, resourceType, name, this.stepKey, tool));

        return this.step(step);
    }

    public ProcessDefinitionUpdateBuilder transform(
            int group,
            String name,
            DataSource dataSource,
            TripleGeoSettings settings,
            int output) {

        this.stepKey++;

        // Step
        Step step = new Step(name,
                             this.stepKey,
                             group,
                             EnumTool.TRIPLE_GEO,
                             EnumOperation.TRANSFORM,
                             new TripleGeoConfiguration(dataSource, settings),
                             output);
        // Output
        this.resources.add(new ProcessOutput(output, EnumResourceType.POI_DATA, name, this.stepKey, EnumTool.TRIPLE_GEO));

        return this.step(step);
    }

    public ProcessDefinitionUpdateBuilder register(
            int group,
            String name,
            ResourceMetadataCreate metadata,
            Integer resource) {
        this.stepKey++;

        // Step
        Step step = new Step(name, this.stepKey, group, EnumTool.CATALOG, EnumOperation.REGISTER, new MetadataRegistrationConfiguration(metadata));
        // Input
        step.getConfiguration().getResources().add(resource);

        return this.step(step);
    }

    public ProcessDefinitionUpdateBuilder setProperties(String name, String descritpion) {
        return this;
    }

    public ProcessDefinitionUpdateBuilder reset() {
        this.stepKey = 0;
        this.resources.clear();
        this.steps.clear();
        return this;
    }

    public ProcessDefinitionUpdate build() {
        return new ProcessDefinitionUpdate(this.resources, this.steps);
    }
}
