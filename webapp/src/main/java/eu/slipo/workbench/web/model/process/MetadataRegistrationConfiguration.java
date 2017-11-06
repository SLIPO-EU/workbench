package eu.slipo.workbench.web.model.process;

import eu.slipo.workbench.web.model.EnumTool;
import eu.slipo.workbench.web.model.resource.ResourceMetadataCreate;

/**
 * Catalog configuration
 */
public class MetadataRegistrationConfiguration extends ToolConfiguration {

    private ResourceMetadataCreate metadata;

    public MetadataRegistrationConfiguration() {
        super(EnumTool.CATALOG);
    }

    public MetadataRegistrationConfiguration(ResourceMetadataCreate metadata) {
        super(EnumTool.CATALOG);
        this.metadata = metadata;
    }

    public ResourceMetadataCreate getMetadata() {
        return metadata;
    }

}
