import * as React from 'react';
import Draggable from 'react-draggable';

import Extend from 'ol/extent';
import GeoJSON from 'ol/format/geojson';
import proj from 'ol/proj';

import {
  ResizableBox,
} from 'react-resizable';

import {
  addClass,
  removeClass,
} from '../../../util';

import {
  OpenLayers,
} from '../../helpers';

import {
  FEATURE_NAME,
  FEATURE_OUTPUT_KEY,
  FEATURE_LAYER_PROPERTY,
} from '../../helpers/map/model/constants';

import {
  EnumPane,
} from '../../../model/map-viewer';

import {
  FeaturePropertyViewer,
  FeatureProvenanceViewer,
  LayerConfig,
} from './';

const styleToKey = (style) => {
  return `${style.symbol}-${style.fill.color}-${style.stroke.color}-${style.stroke.width}-${style.size}-${style.opacity}`;
};

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

  componentDidMount() {
    const body = document.getElementsByTagName('body').item(0);
    addClass(body, 'slipo-map-body');
  }

  componentWillUnmount() {
    const body = document.getElementsByTagName('body').item(0);
    removeClass(body, 'slipo-map-body');
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
            key={`${l.tableName}-${l.color}-${styleToKey(l.style) || ''}`}
            url="/action/proxy/service/wfs"
            version="1.1.0"
            typename={`slipo_eu:${l.tableName}`}
            color={l.color}
            extra={{
              [FEATURE_LAYER_PROPERTY]: l.tableName,
              [FEATURE_NAME]: l.step ? l.step.name : l.resource.name,
              [FEATURE_OUTPUT_KEY]: l.step ? l.step.outputKey : null,
            }}
            style={l.style}
          />
        );
      });

    return layers;
  }

  onStop(e, id) {
    this.props.setItemPosition(id, e.screenX, e.screenY);
  }

  render() {
    const { draggable, layers, selectedFeature, selectedFeatures: features = [], provenance, selectedLayer } = this.props;

    const styles = layers.reduce((result, layer) => ({ ...result, [layer.tableName]: layer.style }), {});

    return (
      <React.Fragment>
        <LayerConfig
          layer={selectedLayer}
          toggle={this.props.toggleLayerConfiguration}
          visible={this.props.layerConfigVisible}
          setLayerStyle={this.props.setLayerStyle}
        />
        <OpenLayers.Map
          minZoom={7}
          maxZoom={19}
          zoom={this.props.initialZoom ? this.props.initialZoom : 13}
          center={this.props.initialCenter ? this.props.initialCenter : this.center}
          className="slipo-map-container-full-screen"
          onMoveEnd={(e) => this.props.onMoveEnd(e)}
        >
          <OpenLayers.Layers>
            {this.getLayers()}
          </OpenLayers.Layers>
          <OpenLayers.Interactions>
            <OpenLayers.Interaction.Select
              onFeatureSelect={this.props.onFeatureSelect}
              styles={styles}
            />
          </OpenLayers.Interactions>
        </OpenLayers.Map>

        {features && features.length !== 0 &&
          <Draggable
            defaultPosition={{ x: draggable[EnumPane.FeatureCollection].left, y: draggable[EnumPane.FeatureCollection].top }}
            onStop={(e) => this.onStop(e, EnumPane.FeatureCollection)}
            handle=".handle"
            offsetParent={document.getElementById('root')}
          >
            <div style={{ pointerEvents: 'none' }}>
              <ResizableBox width={600} height={380} axis="x">
                <FeaturePropertyViewer
                  features={features}
                  layers={layers}
                  selectedFeature={selectedFeature}
                  fetchFeatureProvenance={this.props.fetchFeatureProvenance}
                />
              </ResizableBox>
            </div>
          </Draggable>
        }

        {provenance &&
          <Draggable
            defaultPosition={{ x: draggable[EnumPane.FeatureProvenance].left, y: draggable[EnumPane.FeatureProvenance].top }}
            onStop={(e) => this.onStop(e, EnumPane.FeatureProvenance)}
            handle=".handle"
          >
            <div style={{ pointerEvents: 'none' }}>
              <ResizableBox width={600} height={568} axis="x">
                <FeatureProvenanceViewer
                  provenance={provenance}
                />
              </ResizableBox>
            </div>
          </Draggable>
        }

      </React.Fragment>
    );
  }
}

export default MapViewer;
