package eu.slipo.workbench.web.model.api;

import eu.slipo.workbench.web.model.api.process.TriplegeoApiConfiguration;

public class TransformRequest {

    private String path;

    private TriplegeoApiConfiguration configuration;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public TriplegeoApiConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(TriplegeoApiConfiguration configuration) {
        this.configuration = configuration;
    }

}
