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
  FeatureEvolutionViewer,
  FeaturePropertyViewer,
  FeatureProvenanceViewer,
  LayerConfig,
  FilterForm,
} from './';

const styleToKey = (style) => {
  return `${style.symbol}-${style.fill.color}-${style.stroke.color}-${style.stroke.width}-${style.size}-${style.opacity}`;
};

const filtersToKey = (filters) => {
  return 'filters-' + filters.map(f => `${f.attribute}-${f.type}-${f.value}`).join('-');
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

  getLayers() {
    const { filters = [], baseLayer, bingMaps, osm, layers } = this.props;
    const result = [];

    switch (baseLayer) {
      case 'OSM':
        result.push(
          <OpenLayers.Layer.OSM
            key="osm"
            url={osm.url}
          />
        );
        break;
      case 'BingMaps-Road':
      case 'BingMaps-Aerial':
        if (bingMaps.applicationKey) {
          result.push(
            <OpenLayers.Layer.BingMaps
              key={baseLayer === 'BingMaps-Road' ? 'bing-maps-road' : 'bing-maps-aerial'}
              applicationKey={bingMaps.applicationKey}
              imagerySet={baseLayer === 'BingMaps-Road' ? 'Road' : 'Aerial'}
            />
          );
        }
        break;
    }

    layers
      .filter((l) => !l.hidden)
      .forEach((l) => {
        const key = `${l.tableName}-${l.color}-${styleToKey(l.style) || ''}-${filtersToKey(filters) || ''}`;

        result.push(
          <OpenLayers.Layer.WFS
            key={key}
            url="/action/proxy/service/wfs"
            version="1.1.0"
            typename={`slipo_eu:${l.tableName}`}
            extra={{
              [FEATURE_LAYER_PROPERTY]: l.tableName,
              [FEATURE_NAME]: l.step ? l.step.name : l.resource.name,
              [FEATURE_OUTPUT_KEY]: l.step ? l.step.outputKey : null,
            }}
            filters={filters.filter(f => f.layer === l.tableName)}
            style={l.style}
          />
        );
      });

    return result;
  }

  onStop(data, id) {
    this.props.setItemPosition(id, data.x, data.y);
  }

  toggleEditor(feature = null) {
    if (feature) {
      // Zoom to feature
      const extent = feature.getGeometry().getExtent();
      const center = Extend.getCenter(extent);
      this._map.moveTo(center, 15);
      // Enable modify interaction
      this.props.toggleEditor(feature);
    }
  }

  renderPanels() {
    const {
      draggable, draggableOrder, editActive, layers,
      selectedFeature, selectedFeatures: features = [],
      onEvolutionGeometryChange, onEvolutionUpdatesToggle, onProvenanceGeometryChange,
      evolution, provenance, viewport,
      viewRevision,
    } = this.props;

    const panels = [];
    const bounds = { left: 0, top: 100, right: viewport.width - 250, bottom: viewport.height - 250 };

    draggableOrder.forEach(id => {
      switch (id) {
        case EnumPane.FeatureCollection:
          if (features && features.length !== 0) {
            panels.push(
              <div key={id} onClick={() => this.props.bringToFront(id)}>
                <Draggable
                  defaultPosition={{ x: draggable[id].left, y: draggable[id].top }}
                  onStop={(e, data) => this.onStop(data, id)}
                  handle=".handle"
                  bounds={bounds}
                >
                  <div style={{ pointerEvents: 'none' }}>
                    <ResizableBox width={600} height={380} axis="x" minConstraints={[600, 380]}>
                      <FeaturePropertyViewer
                        editActive={editActive}
                        features={features}
                        layers={layers}
                        selectedFeature={selectedFeature}
                        fetchFeatureEvolution={this.props.fetchFeatureEvolution}
                        fetchFeatureProvenance={this.props.fetchFeatureProvenance}
                        close={() => {
                          if (!provenance && !evolution) {
                            // If evolution/provenance floating panels are hidden, clear selection
                            this._select.clear();
                          }
                          this.props.clearSelectedFeatures();
                        }}
                        toggleEditor={() => this.toggleEditor(selectedFeature.feature)}
                      />
                    </ResizableBox>
                  </div>
                </Draggable>
              </div>
            );
          }
          break;

        case EnumPane.FeatureProvenance:
          if (provenance) {
            panels.push(
              <div key={id} onClick={() => this.props.bringToFront(id)}>
                <Draggable
                  defaultPosition={{ x: draggable[id].left, y: draggable[id].top }}
                  onStop={(e, data) => this.onStop(data, id)}
                  handle=".handle"
                  bounds={bounds}
                >
                  <div style={{ pointerEvents: 'none' }}>
                    <ResizableBox width={600} height={568} axis="x" minConstraints={[600, 568]}>
                      <FeatureProvenanceViewer
                        close={() => {
                          if (!features || features.length === 0) {
                            // If property viewer floating panel is hidden, clear selection
                            this._select.clear();
                          }
                          this.props.hideProvenance();
                        }}
                        editActive={editActive}
                        onProvenanceGeometryChange={onProvenanceGeometryChange}
                        provenance={provenance}
                        toggleEditor={() => this.toggleEditor(selectedFeature.feature)}
                      />
                    </ResizableBox>
                  </div>
                </Draggable>
              </div>
            );
          }
          break;

        case EnumPane.FeatureEvolution:
          if (evolution) {
            panels.push(
              <div key={id} onClick={() => this.props.bringToFront(id)}>
                <Draggable
                  defaultPosition={{ x: draggable[id].left, y: draggable[id].top }}
                  onStop={(e, data) => this.onStop(data, id)}
                  handle=".handle"
                  bounds={bounds}
                >
                  <div style={{ pointerEvents: 'none' }}>
                    <ResizableBox width={600} height={568} axis="x" minConstraints={[600, 568]}>
                      <FeatureEvolutionViewer
                        close={() => {
                          if (!features || features.length === 0) {
                            // If property viewer floating panel is hidden, clear selection
                            this._select.clear();
                          }
                          this.props.hideEvolution();
                        }}
                        editActive={editActive}
                        evolution={evolution}
                        onEvolutionGeometryChange={onEvolutionGeometryChange}
                        onEvolutionUpdatesToggle={onEvolutionUpdatesToggle}
                        viewRevision={viewRevision}
                      />
                    </ResizableBox>
                  </div>
                </Draggable>
              </div>
            );
          }
      }
    });

    return panels;
  }

  render() {
    const { layers, selectedLayer } = this.props;
    const styles = layers.reduce((result, layer) => ({ ...result, [layer.tableName]: layer.style }), {});

    return (
      <React.Fragment>
        <LayerConfig
          layer={selectedLayer}
          toggle={this.props.toggleLayerConfiguration}
          visible={this.props.layerConfigVisible}
          setLayerStyle={this.props.setLayerStyle}
        />
        <FilterForm
          filters={this.props.filters}
          layers={layers}
          toggle={this.props.toggleFilterForm}
          search={this.props.setFilter}
          visible={this.props.filterFormVisible}
        />
        <OpenLayers.Map
          ref={(component) => { this._map = component; }}
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
              ref={(component) => { this._select = component; }}
              active={!this.props.editActive}
              onFeatureSelect={this.props.onFeatureSelect}
              styles={styles}
            />
            <OpenLayers.Interaction.Modify
              active={this.props.editActive}
              onGeometryChange={this.props.onGeometryChange}
              feature={this.props.editFeature}
              styles={styles}
            />
          </OpenLayers.Interactions>
        </OpenLayers.Map>

        {this.renderPanels()}

      </React.Fragment>
    );
  }
}

export default MapViewer;
