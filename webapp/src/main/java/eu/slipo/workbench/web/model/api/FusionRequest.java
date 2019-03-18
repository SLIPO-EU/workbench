package eu.slipo.workbench.web.model.api;

public class FusionRequest {

    private Input left;
    private Input right;
    private Input links;

    private String profile;

    public Input getLeft() {
        return left;
    }

    public void setLeft(Input left) {
        this.left = left;
    }

    public Input getRight() {
        return right;
    }

    public void setRight(Input right) {
        this.right = right;
    }

    public Input getLinks() {
        return links;
    }

    public void setLinks(Input links) {
        this.links = links;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

}
