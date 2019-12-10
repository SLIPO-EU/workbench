package eu.slipo.workbench.web.model.configuration;

import java.util.Map;

import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.tool.ToolConfiguration;

/**
 * Application configuration settings
 */
public class ClientConfiguration {

    private Map<EnumTool, Map<String, ToolConfiguration<?>>> profiles;

    private Map<EnumTool, Map<String, String>> profileComments;

    private OsmConfiguration osm;

    private BingMapsConfiguration bingMaps;

    private TripleGeoConfiguration tripleGeo;

    private ReverseTripleGeoConfiguration reverseTripleGeo;

    private LimesConfiguration limes;

    private FagiConfiguration fagi;

    private DeerConfiguration deer;

    private MapDefaults mapDefaults;

    private MapViewerConfiguration mapViewer;

    public Map<EnumTool, Map<String, ToolConfiguration<?>>> getProfiles() {
        return profiles;
    }

    public void setProfiles(Map<EnumTool, Map<String, ToolConfiguration<?>>> profiles) {
        this.profiles = profiles;
    }

    public Map<EnumTool, Map<String, String>> getProfileComments() {
        return profileComments;
    }

    public void setProfileComments(Map<EnumTool, Map<String, String>> profileComments) {
        this.profileComments = profileComments;
    }

    public OsmConfiguration getOsm() {
        return osm;
    }

    public void setOsm(OsmConfiguration osm) {
        this.osm = osm;
    }

    public BingMapsConfiguration getBingMaps() {
        return bingMaps;
    }

    public void setBingMaps(BingMapsConfiguration bingMaps) {
        this.bingMaps = bingMaps;
    }

    public TripleGeoConfiguration getTripleGeo() {
        return tripleGeo;
    }

    public void setTripleGeo(TripleGeoConfiguration tripleGeo) {
        this.tripleGeo = tripleGeo;
    }

    public ReverseTripleGeoConfiguration getReverseTripleGeo() {
        return reverseTripleGeo;
    }

    public void setReverseTripleGeo(ReverseTripleGeoConfiguration reverseTripleGeo) {
        this.reverseTripleGeo = reverseTripleGeo;
    }

    public LimesConfiguration getLimes() {
        return limes;
    }

    public void setLimes(LimesConfiguration limes) {
        this.limes = limes;
    }

    public FagiConfiguration getFagi() {
        return fagi;
    }

    public void setFagi(FagiConfiguration fagi) {
        this.fagi = fagi;
    }

    public DeerConfiguration getDeer() {
        return deer;
    }

    public void setDeer(DeerConfiguration deer) {
        this.deer = deer;
    }

    public MapDefaults getMapDefaults() {
        return mapDefaults;
    }

    public void setMapDefaults(MapDefaults mapDefaults) {
        this.mapDefaults = mapDefaults;
    }

    public MapViewerConfiguration getMapViewer() {
        return mapViewer;
    }

    public void setMapViewer(MapViewerConfiguration mapViewer) {
        this.mapViewer = mapViewer;
    }

}
