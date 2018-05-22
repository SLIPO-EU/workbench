package eu.slipo.workbench.web.model.configuration;

import java.util.Map;

import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.tool.ToolConfiguration;

/**
 * Application configuration settings
 */
public class ClientConfiguration {

    private Map<EnumTool, Map<String, ToolConfiguration>> profiles;

    private OsmConfiguration osm;

    private BingMapsConfiguration bingMaps;

    private TripleGeoConfiguration tripleGeo;

    private LimesConfiguration limes;

    public Map<EnumTool, Map<String, ToolConfiguration>> getProfiles() {
        return profiles;
    }

    public void setProfiles(Map<EnumTool, Map<String, ToolConfiguration>> profiles) {
        this.profiles = profiles;
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

    public LimesConfiguration getLimes() {
        return limes;
    }

    public void setLimes(LimesConfiguration limes) {
        this.limes = limes;
    }

}
