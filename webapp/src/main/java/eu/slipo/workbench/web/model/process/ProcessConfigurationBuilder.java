package eu.slipo.workbench.web.model.process;

import java.util.ArrayList;
import java.util.List;

import eu.slipo.workbench.web.model.EnumOperation;
import eu.slipo.workbench.web.model.EnumTool;
import eu.slipo.workbench.web.model.ResourceMetadataRegistration;
import eu.slipo.workbench.web.model.ResourceRegistration;

public class ProcessConfigurationBuilder {

    private int stepIndex = 0;

    private List<ProcessResource> resources = new ArrayList<ProcessResource>();

    private List<Step> steps = new ArrayList<Step>();

    private List<Integer> execution = new ArrayList<Integer>();

    public ProcessConfigurationBuilder resource(ProcessResource resource) {
        this.resources.add(resource);
        return this;
    }

    public ProcessConfigurationBuilder fileResource(int index, String filename) {
        this.resources.add(new FileProcessResource(index, filename));

        return this;
    }

    public ProcessConfigurationBuilder transientResource(int index, ResourceRegistration registration) {
        this.resources.add(new TransientProcessResource(index, registration));

        return this;
    }

    private ProcessConfigurationBuilder step(Step step) {
        this.steps.add(step);
        this.execution.add(step.getIndex());

        return this;
    }

    public ProcessConfigurationBuilder step(EnumTool tool, EnumOperation operation, ToolConfiguration configuration,
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

    public ProcessConfigurationBuilder step(EnumTool tool, EnumOperation operation, ToolConfiguration configuration,
            List<Integer> resources, int output) {

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

    public ProcessConfigurationBuilder transform(TripleGeoConfiguration configuration, Integer resource, int output) {
        this.stepIndex++;

        // Step
        Step step = new Step(this.stepIndex, EnumTool.TRIPLE_GEO, EnumOperation.TRANSFORM, configuration, output);
        // Input
        step.getConfiguration().getResources().add(resource);
        // Output
        this.resources.add(new OutputProcessResource(output, this.stepIndex));

        return this.step(step);
    }

    public ProcessConfigurationBuilder register(ResourceMetadataRegistration metadata, Integer resource) {
        this.stepIndex++;

        // Step
        Step step = new Step(this.stepIndex, EnumTool.CATALOG, EnumOperation.REGISTER, new MetadataRegistrationConfiguration(metadata));
        // Input
        step.getConfiguration().getResources().add(resource);

        return this.step(step);
    }

    public ProcessConfigurationBuilder reset() {
        this.stepIndex = 0;
        this.resources.clear();
        this.steps.clear();
        this.execution.clear();
        return this;
    }

    public ProcessConfiguration build() {
        return new ProcessConfiguration(this.resources, this.steps, this.execution);
    }
}
