package eu.slipo.workbench.web.model.api;

public class InterlinkRequest {

    private Input left;
    private Input right;
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

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

}
