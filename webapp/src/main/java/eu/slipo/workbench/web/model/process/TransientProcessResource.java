package eu.slipo.workbench.web.model.process;

import eu.slipo.workbench.web.model.ResourceRegistration;

public class TransientProcessResource extends ProcessResource {

    private ResourceRegistration registration;

    public TransientProcessResource() {
        super();
        this.type = EnumProcessResource.TRANSIENT;
    }

    public TransientProcessResource(int index, ResourceRegistration registration) {
        super(index, EnumProcessResource.TRANSIENT);
        this.registration = registration;
    }

    public ResourceRegistration getRegistration() {
        return registration;
    }

}
