package eu.slipo.workbench.web.model.configuration;

public class MapViewerConfiguration {

    private Integer layerMaxZoom;

    public Integer getLayerMaxZoom() {
        if (layerMaxZoom == null) {
            return 15;
        }
        return layerMaxZoom;
    }

    public void setLayerMaxZoom(Integer layerMaxZoom) {
        this.layerMaxZoom = layerMaxZoom;
    }

}
