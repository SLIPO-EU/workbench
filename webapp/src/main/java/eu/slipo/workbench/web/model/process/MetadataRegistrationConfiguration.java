package eu.slipo.workbench.web.model.process;

import eu.slipo.workbench.web.model.EnumTool;
import eu.slipo.workbench.web.model.ResourceMetadataRegistration;

public class MetadataRegistrationConfiguration extends ToolConfiguration {

    private ResourceMetadataRegistration metadata;

    public MetadataRegistrationConfiguration() {
        super();
        this.tool = EnumTool.CATALOG;
    }

    public MetadataRegistrationConfiguration(ResourceMetadataRegistration metadata) {
        super();
        this.tool = EnumTool.CATALOG;
        this.metadata = metadata;
    }

    public ResourceMetadataRegistration getMetadata() {
        return metadata;
    }

}
