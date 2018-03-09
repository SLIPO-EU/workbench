package eu.slipo.workbench.web.model;

import eu.slipo.workbench.web.model.configuration.BingMapsConfiguration;
import eu.slipo.workbench.web.model.configuration.OsmConfiguration;

/**
 * Application configuration settings
 */
public class Configuration {

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
