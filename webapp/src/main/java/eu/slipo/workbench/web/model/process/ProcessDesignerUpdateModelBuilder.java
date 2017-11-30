package eu.slipo.workbench.web.model.process;

import java.util.ArrayList;
import java.util.List;

import eu.slipo.workbench.web.model.EnumOperation;
import eu.slipo.workbench.web.model.EnumTool;
import eu.slipo.workbench.web.model.resource.DataSource;
import eu.slipo.workbench.web.model.resource.ResourceMetadataCreate;

/**
 * Helper builder for composing a {@link ProcessDesignerUpdateModel} instance
 */
public class ProcessDesignerUpdateModelBuilder {

    private int stepIndex = 0;

    private List<ProcessResource> resources = new ArrayList<ProcessResource>();

    private List<Step> steps = new ArrayList<Step>();

    private ProcessDesignerUpdateModelBuilder() {

    }

    public static ProcessDesignerUpdateModelBuilder create() {
        return new ProcessDesignerUpdateModelBuilder();
    }

    public ProcessDesignerUpdateModelBuilder resource(ProcessResource resource) {
        this.resources.add(resource);
        return this;
    }

    private ProcessDesignerUpdateModelBuilder step(Step step) {
        this.steps.add(step);

        return this;
    }

    public ProcessDesignerUpdateModelBuilder step(
            EnumTool tool,
            EnumOperation operation,
            ToolConfiguration configuration,
            List<Integer> resources) {

        this.stepIndex++;

        // Step
        Step step = new Step(this.stepIndex, tool, operation, configuration);
        // Input
        for (Integer r : resources) {
            step.getConfiguration().getResources().add(r);
        }

        return this.step(step);
    }

    public ProcessDesignerUpdateModelBuilder step(
            EnumTool tool,
            EnumOperation operation,
            ToolConfiguration configuration,
            List<Integer> resources,
            int output) {

        this.stepIndex++;

        // Step
        Step step = new Step(this.stepIndex, tool, operation, configuration, output);
        // Input
        for (Integer r : resources) {
            step.getConfiguration().getResources().add(r);
        }
        // Output
        this.resources.add(new OutputProcessResource(output, this.stepIndex));

        return this.step(step);
    }

    public ProcessDesignerUpdateModelBuilder transform(
            DataSource dataSource,
            TripleGeoSettings settings,
            int output) {

        this.stepIndex++;

        // Step
        Step step = new Step(this.stepIndex,
                             EnumTool.TRIPLE_GEO,
                             EnumOperation.TRANSFORM,
                             new TripleGeoConfiguration(dataSource, settings),
                             output);
        // Output
        this.resources.add(new OutputProcessResource(output, this.stepIndex));

        return this.step(step);
    }

    public ProcessDesignerUpdateModelBuilder register(ResourceMetadataCreate metadata, Integer resource) {
        this.stepIndex++;

        // Step
        Step step = new Step(this.stepIndex, EnumTool.CATALOG, EnumOperation.REGISTER, new MetadataRegistrationConfiguration(metadata));
        // Input
        step.getConfiguration().getResources().add(resource);

        return this.step(step);
    }

    public ProcessDesignerUpdateModelBuilder reset() {
        this.stepIndex = 0;
        this.resources.clear();
        this.steps.clear();
        return this;
    }

    public ProcessDesignerUpdateModel build() {
        return new ProcessDesignerUpdateModel(this.resources, this.steps);
    }
}
