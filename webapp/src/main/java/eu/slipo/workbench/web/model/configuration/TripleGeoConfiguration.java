package eu.slipo.workbench.web.model.configuration;

import java.util.List;

public class TripleGeoConfiguration {

    private String version;

    /**
     * Default version for existing TripleGeo steps.
     *
     * New TripleGeo steps in existing processes are either have their version set to the
     * one of existing TripleGeo steps or set to the {@link #version}.
     */
    private String baselineVersion;

    private List<String> supportedVersions;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBaselineVersion() {
        return baselineVersion;
    }

    public void setBaselineVersion(String baselineVersion) {
        this.baselineVersion = baselineVersion;
    }

    public List<String> getSupportedVersions() {
        return supportedVersions;
    }

    public void setSupportedVersions(List<String> supportedVersions) {
        this.supportedVersions = supportedVersions;
    }

}
