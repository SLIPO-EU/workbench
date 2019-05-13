package eu.slipo.workbench.web.model.api;

import eu.slipo.workbench.web.model.api.process.ReverseTriplegeoApiConfiguration;

public class ExportRequest {

    private Input input;

    private ReverseTriplegeoApiConfiguration configuration;

    public Input getInput() {
        return input;
    }

    public void setInput(Input input) {
        this.input = input;
    }

    public ReverseTriplegeoApiConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ReverseTriplegeoApiConfiguration configuration) {
        this.configuration = configuration;
    }

}
