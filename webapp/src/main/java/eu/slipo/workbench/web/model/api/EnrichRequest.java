package eu.slipo.workbench.web.model.api;

public class EnrichRequest {

    private Input input;
    private String profile;

    public Input getInput() {
        return input;
    }

    public void setInput(Input input) {
        this.input = input;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

}
