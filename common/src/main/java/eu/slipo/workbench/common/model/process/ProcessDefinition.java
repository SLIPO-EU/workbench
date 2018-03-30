package eu.slipo.workbench.common.model.process;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.slipo.workbench.common.model.resource.ResourceIdentifier;

public class ProcessDefinition implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final String name;

    private String description;

    private final List<ProcessInput> resources;

    private final List<Step> steps;

    /**
     * Map a step key to a step descriptor
     */
    private final Map<Integer, Step> keyToStep;

    /**
     * Map a step name to a step descriptor
     */
    private final Map<String, Step> nameToStep;

    private final Map<Integer, ProcessInput> resourceKeyToResource;

    /**
     * Map a resource key to the step key of a processing step which produces it as an output
     */
    private final Map<Integer, Integer> resourceKeyToStepKey;

    /**
     * Map a resource key to the identifier of a catalog resource
     */
    private final Map<Integer, ResourceIdentifier> resourceKeyToResourceIdentifier;

    @JsonCreator
    protected ProcessDefinition(
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("resources") List<ProcessInput> resources,
        @JsonProperty("steps") List<Step> steps)
    {
        Assert.isTrue(!StringUtils.isEmpty(name), "A non-empty name is required");
        Assert.notNull(resources, "A list of resources is required");
        Assert.notNull(steps, "A list of steps is required");

        this.name = name;
        this.description = description;
        this.resources = Collections.unmodifiableList(resources);
        this.steps = Collections.unmodifiableList(steps);

        this.keyToStep = Collections.unmodifiableMap(
            steps.stream().collect(Collectors.toMap(s -> s.key(), Function.identity())));

        this.nameToStep = Collections.unmodifiableMap(
            steps.stream().collect(Collectors.toMap(s -> s.name(), Function.identity())));

        this.resourceKeyToResource = Collections.unmodifiableMap(
            resources.stream().collect(Collectors.toMap(r -> r.key(), Function.identity())));

        this.resourceKeyToResourceIdentifier = Collections.unmodifiableMap(
            resources.stream()
                .filter(r -> r instanceof CatalogResource)
                .collect(Collectors.toMap(r -> r.key(), r -> ((CatalogResource) r).resourceIdentifier())));

        this.resourceKeyToStepKey = Collections.unmodifiableMap(
            resources.stream()
                .filter(r -> r instanceof ProcessOutput)
                .collect(Collectors.toMap(r -> r.key(), r -> ((ProcessOutput) r).stepKey())));
    }

    protected ProcessDefinition(String name, List<ProcessInput> resources, List<Step> steps)
    {
       this(name, null, resources, steps);
    }


    @JsonProperty("name")
    public String name()
    {
        return name;
    }

    @JsonProperty("description")
    public String description()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * List input resources
     */
    @JsonProperty("resources")
    public List<ProcessInput> resources()
    {
        return resources;
    }

    /**
     * List processing steps
     */
    @JsonProperty("steps")
    public List<Step> steps()
    {
        return steps;
    }

    /**
     * Get a {@link Step} descriptor by step key
     * @param stepKey The step key
     */
    @JsonIgnore
    public Step stepByKey(int stepKey)
    {
        return keyToStep.get(stepKey);
    }

    /**
     * Get a {@link Step} descriptor by step name
     *
     * @param stepName The name of a step
     */
    @JsonIgnore
    public Step stepByName(String stepName)
    {
        return stepName == null? null : nameToStep.get(stepName);
    }

    /**
     * Get a resource by its key
     *
     * @param resourceKey The resource key
     * @return
     */
    @JsonIgnore
    public ProcessInput resourceByResourceKey(int resourceKey)
    {
        return resourceKeyToResource.get(resourceKey);
    }

    /**
     * Get a {@link Step} descriptor by a resource key
     *
     * @param resourceKey The resource key assigned to the <em>output</em> of a step
     * @return a {@link Step} descriptor if the given key corresponds to an output,
     *   else <tt>null</tt> (i.e when the key is not not known, or it corresponds to
     *   other a non-output kind of resource)
     */
    @JsonIgnore
    public Step stepByResourceKey(int resourceKey)
    {
        Integer stepKey = resourceKeyToStepKey.get(resourceKey);
        return stepKey == null? null : keyToStep.get(stepKey);
    }

    /**
     * Get a {@link ResourceIdentifier} by a resource key
     *
     * @param resourceKey The resource key assigned to a catalog resource
     * @return a {@link ResourceIdentifier} identifier if the given key corresponds to
     *   a previously defined catalog resource, else <tt>null</tt>.
     */
    @JsonIgnore
    public ResourceIdentifier resourceIdentifierByResourceKey(int resourceKey)
    {
        return resourceKeyToResourceIdentifier.get(resourceKey);
    }

    /**
     * The subset of resource keys that correspond to an output of a processing step
     * (i.e correspond to a resource of type {@link ProcessOutput}).
     * @return A set of keys
     */
    @JsonIgnore
    public Set<Integer> outputKeys()
    {
        return resourceKeyToStepKey.keySet();
    }

    public static void remapKeys(ProcessDefinition definition) {
        Map<Integer, Integer> mapping = new HashMap<Integer, Integer>();

        definition.steps()
        .stream()
        .forEach(s-> {
           if(!mapping.containsKey(s.key())) {
                mapping.put(s.key(), mapping.size());
           }
        });

        // Map resources
        definition.resources()
            .stream()
            .forEach(r -> {
                if (r.getInputType() == EnumInputType.OUTPUT) {
                    ProcessOutput typedResource = (ProcessOutput) r;
                    typedResource.stepKey = mapping.get(typedResource.stepKey());
                }
            });

        // Map steps
        definition.steps()
            .stream()
            .forEach(s -> {
                s.key = mapping.get(s.key());
            });
    }
}
