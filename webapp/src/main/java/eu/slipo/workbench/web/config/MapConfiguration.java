package eu.slipo.workbench.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import eu.slipo.workbench.web.model.configuration.BingMapsConfiguration;
import eu.slipo.workbench.web.model.configuration.GeoServerConfiguration;
import eu.slipo.workbench.web.model.configuration.MapDefaults;
import eu.slipo.workbench.web.model.configuration.MapViewerConfiguration;
import eu.slipo.workbench.web.model.configuration.OsmConfiguration;

@Configuration
@PropertySource("classpath:config/map.properties")
@ConfigurationProperties()
public class MapConfiguration {

    private OsmConfiguration osm;

    private GeoServerConfiguration geoServer;

    private BingMapsConfiguration bingMaps;

    private MapDefaults defaults;

    private MapViewerConfiguration mapViewer;

    public OsmConfiguration getOsm() {
        return osm;
    }

    public void setOsm(OsmConfiguration osm) {
        this.osm = osm;
    }

    public GeoServerConfiguration getGeoServer() {
        return geoServer;
    }

    public void setGeoServer(GeoServerConfiguration geoServer) {
        this.geoServer = geoServer;
    }

    public BingMapsConfiguration getBingMaps() {
        return bingMaps;
    }

    public void setBingMaps(BingMapsConfiguration bingMaps) {
        this.bingMaps = bingMaps;
    }

    public MapDefaults getDefaults() {
        return defaults;
    }

    public void setDefaults(MapDefaults defaults) {
        this.defaults = defaults;
    }

    public MapViewerConfiguration getMapViewer() {
        return mapViewer;
    }

    public void setMapViewer(MapViewerConfiguration mapViewer) {
        this.mapViewer = mapViewer;
    }

}
