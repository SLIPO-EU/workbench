package eu.slipo.workbench.web.model.configuration;

/**
 * Application configuration settings
 */
public class ClientConfiguration {

    private OsmConfiguration osm;

    private BingMapsConfiguration bingMaps;

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

}
