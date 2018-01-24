import Map from './map';
import Layers from './layers/layers';
import OsmLayer from './layers/osm-layer';
import BingMapsLayer from './layers/bing-maps-layer';
import WfsLayer from './layers/wfs-layer';
import GeoJsonLayer from './layers/geojson-layer';
import Interactions from './interactions/interactions';
import Select from './interactions/select';

export default {
  Map,
  Layers,
  Layer: {
    OSM: OsmLayer,
    BingMaps: BingMapsLayer,
    WFS: WfsLayer,
    GeoJSON: GeoJsonLayer,
  },
  Interactions,
  Interaction: {
    Select,
  }
};
