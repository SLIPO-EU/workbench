package eu.slipo.workbench.web.model.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:config/map.properties")
@ConfigurationProperties()
public class MapProperties {

    private OsmConfiguration osm;

    private GeoServerConfiguration geoServer;

    private BingMapsConfiguration bingMaps;

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

}
