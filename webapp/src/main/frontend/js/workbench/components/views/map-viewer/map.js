import * as React from 'react';

import Extend from 'ol/extent';
import GeoJSON from 'ol/format/geojson';
import proj from 'ol/proj';

import {
  OpenLayers,
} from '../../helpers';

import {
  default as ItemContainer,
} from './item-container';

import {
  default as DraggableItem,
} from './draggable-item';

import {
  default as FeaturePropertyViewer,
} from './feature-property-viewer';

/**
 * Renders a map from the input/output datasets of a single process
 * execution instance
 *
 * @class ProcessExecutionMapViewer
 * @extends {React.Component}
 */
class MapViewer extends React.Component {

  constructor(props) {
    super(props);

    this.onFeatureSelect = this.onFeatureSelect.bind(this);
  }

  get center() {
    const { defaultCenter, layers } = this.props;
    const extent = Extend.createEmpty();
    const format = new GeoJSON();

    layers
      .filter((l) => l.boundingBox)
      .forEach((l) => {
        const bbox = format.readFeature(l.boundingBox);
        Extend.extend(extent, bbox.getGeometry().getExtent());
      });

    return (Extend.isEmpty(extent) ? proj.fromLonLat(defaultCenter) : proj.fromLonLat(Extend.getCenter(extent)));
  }

  onFeatureSelect(features) {
    this.props.selectFeatures(features);
  }

  getLayers() {
    const layers = [];

    switch (this.props.baseLayer) {
      case 'OSM':
        layers.push(
          <OpenLayers.Layer.OSM
            key="osm"
            url={this.props.osm.url}
          />
        );
        break;
      case 'BingMaps-Road':
      case 'BingMaps-Aerial':
        if (this.props.bingMaps.applicationKey) {
          layers.push(
            <OpenLayers.Layer.BingMaps
              key={this.props.baseLayer === 'BingMaps-Road' ? 'bing-maps-road' : 'bing-maps-aerial'}
              applicationKey={this.props.bingMaps.applicationKey}
              imagerySet={this.props.baseLayer === 'BingMaps-Road' ? 'Road' : 'Aerial'}
            />
          );
        }
        break;
    }

    this.props.layers
      .filter((l) => !l.hidden)
      .forEach((l) => {
        layers.push(
          <OpenLayers.Layer.WFS
            key={`${l.tableName}-${l.color}-${l.icon || ''}`}
            url="/action/proxy/service/wfs"
            version="1.1.0"
            typename={`slipo_eu:${l.tableName}`}
            color={l.color}
            icon={l.icon}
          />
        );
      });

    return layers;
  }

  render() {
    const { selectedFeatures: features = [] } = this.props;

    return (
      <div>
        <ItemContainer>
          <OpenLayers.Map
            minZoom={9}
            maxZoom={18}
            zoom={19}
            center={this.center}
            className="slipo-map-container-full-screen"
            onMoveEnd={(e) => this.props.onMoveEnd(e)}
          >
            <OpenLayers.Layers>
              {this.getLayers()}
            </OpenLayers.Layers>
            <OpenLayers.Interactions>
              <OpenLayers.Interaction.Select
                onFeatureSelect={this.props.onFeatureSelect}
                multi={true}
                width={2}
              />
            </OpenLayers.Interactions>
          </OpenLayers.Map>
          <DraggableItem id="FeatureTable" left={220} top={120}>
            <FeaturePropertyViewer
              features={features}
            />
          </DraggableItem>
        </ItemContainer>
      </div>
    );
  }
}

export default MapViewer;
