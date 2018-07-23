import * as React from 'react';
import * as ReactRedux from 'react-redux';

import Extend from 'ol/extent';
import GeoJSON from 'ol/format/geojson';
import proj from 'ol/proj';

import {
  bindActionCreators
} from 'redux';

import {
  toast
} from 'react-toastify';

import {
  OpenLayers,
  ToastTemplate,
} from '../../components/helpers';

import {
  fetchExecutionDetails,
  reset,
  selectFeatures,
} from '../../ducks/ui/views/process-designer';

import {
  StaticRoutes,
} from '../../model';

/**
 * Browse POI data and POI data integration workflow results
 *
 * @class ProcessExecutionMapViewer
 * @extends {React.Component}
 */
class ProcessExecutionMapViewer extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      isLoading: true,
    };

    this.onFetchError = this.onFetchError.bind(this);
    this.onFetchSuccess = this.onFetchSuccess.bind(this);
    this.onFeatureSelect = this.onFeatureSelect.bind(this);
  }

  get center() {
    const extent = Extend.createEmpty();
    const format = new GeoJSON();

    this.props.layers
      .filter((l) => l.boundingBox)
      .forEach((l) => {
        const bbox = format.readFeature(l.boundingBox);
        Extend.extend(extent, bbox.getGeometry().getExtent());
      });

    return proj.fromLonLat(Extend.getCenter(extent));
  }

  componentDidMount() {
    const { id, version, execution, ...rest } = this.props.match.params;

    this.props.reset();
    this.props.fetchExecutionDetails(Number.parseInt(id), Number.parseInt(version), Number.parseInt(execution))
      .then(this.onFetchSuccess)
      .catch(this.onFetchError);
  }

  onFetchSuccess() {
    this.setState({ isLoading: false });
  }

  onFetchError(err) {
    this.error(err.message);
  }

  onFeatureSelect(features) {
    this.props.selectFeatures(features);
  }

  error(message, redirect) {
    toast.dismiss();

    toast.error(
      <ToastTemplate iconClass='fa-warning' text={message} />
    );

    if ((typeof redirect === 'undefined') || (redirect)) {
      setTimeout(() => this.props.history.push(StaticRoutes.ProcessExecutionExplorer), 500);
    }
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
    if (this.state.isLoading) {
      return null;
    }
    return (
      <div className="animated fadeIn">
        <OpenLayers.Map minZoom={13} maxZoom={18} zoom={15} center={this.center} className="slipo-map-container-full-screen">
          <OpenLayers.Layers>
            {this.getLayers()}
          </OpenLayers.Layers>
          <OpenLayers.Interactions>
            <OpenLayers.Interaction.Select
              onFeatureSelect={this.onFeatureSelect}
              multi={true}
              width={2}
            />
          </OpenLayers.Interactions>
        </OpenLayers.Map>
      </div>
    );
  }
}

const mapStateToProps = (state) => ({
  baseLayer: state.ui.views.process.designer.execution.baseLayer,
  bingMaps: state.config.bingMaps,
  layers: state.ui.views.process.designer.execution.layers,
  osm: state.config.osm,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  fetchExecutionDetails,
  reset,
  selectFeatures,
}, dispatch);

const mergeProps = (stateProps, dispatchProps, ownProps) => {
  return {
    ...stateProps,
    ...dispatchProps,
    ...ownProps,
  };
};

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps, mergeProps)(ProcessExecutionMapViewer);
