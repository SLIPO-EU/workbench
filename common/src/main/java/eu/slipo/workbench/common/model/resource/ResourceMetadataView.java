package eu.slipo.workbench.common.model.resource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Resource metadata view model
 */
public class ResourceMetadataView 
{
    /**
     * A user-provided name for the resource
     */
    private final String name;

    /**
     * A user-provided description for the resource
     */
    private final String description;

    @JsonCreator
    public ResourceMetadataView(@JsonProperty String name, @JsonProperty String description) 
    {
        this.name = name;
        this.description = description;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public String getDescription() {
        return description;
    }
}
