import * as React from 'react';
import * as ReactRedux from 'react-redux';

import {
  bindActionCreators
} from 'redux';

import {
  fetchFeatureProvenance,
  fetchResourceMapData,
  reset,
  selectFeatures,
  setCenter,
  setItemPosition,
  setLayerStyle,
  toggleLayerConfiguration,
} from '../../ducks/ui/views/map-viewer';

import {
  message,
} from '../../service';

import {
  MapViewer,
} from './map-viewer';

/**
 * Renders a map from the input/output datasets of the process execution
 * instance that generated the selected resource revision
 *
 * @class ResourceMapViewer
 * @extends {React.Component}
 */
class ResourceMapViewer extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      isLoading: true,
    };

    this.onFetchError = this.onFetchError.bind(this);
    this.onFetchSuccess = this.onFetchSuccess.bind(this);
    this.onMoveEnd = this.onMoveEnd.bind(this);
  }

  get params() {
    const { id, version } = this.props.match.params;

    return {
      id: Number.parseInt(id),
      version: Number.parseInt(version),
    };
  }

  componentDidMount() {
    const params = this.params;
    const { resource, version } = this.props;

    if ((!resource) || (resource.id !== params.id) || (version !== params.version)) {
      this.props.reset();
      this.props.fetchResourceMapData(params.id, params.version)
        .then(this.onFetchSuccess)
        .catch(this.onFetchError);
    } else {
      this.onFetchSuccess();
    }
  }

  onFetchSuccess() {
    this.setState({ isLoading: false });
  }

  onFetchError(err) {
    message.error(err.message, 'fa-warning');

    setTimeout(() => this.props.history.goBack(), 500);
  }

  onMoveEnd(data) {
    this.props.setCenter(data.center, data.zoom);
  }

  render() {
    if (this.state.isLoading) {
      return null;
    }

    const {
      baseLayer,
      bingMaps,
      defaultCenter,
      fetchFeatureProvenance,
      layers,
      osm,
      resource: { execution },
    } = this.props;

    return (
      <MapViewer
        baseLayer={baseLayer}
        bingMaps={bingMaps}
        defaultCenter={defaultCenter}
        draggable={this.props.draggable}
        fetchFeatureProvenance={
          (outputKey, featureId, featureUri) => fetchFeatureProvenance(
            execution.id,
            execution.version,
            execution.execution,
            outputKey,
            featureId,
            featureUri)
        }
        initialCenter={this.props.initialCenter}
        initialZoom={this.props.initialZoom}
        layerConfigVisible={this.props.layerConfigVisible}
        layers={layers}
        onFeatureSelect={(features) => this.props.selectFeatures(features)}
        onMoveEnd={this.onMoveEnd}
        osm={osm}
        provenance={this.props.provenance}
        selectedFeature={this.props.selectedFeature}
        selectedFeatures={this.props.selectedFeatures}
        selectedLayer={this.props.selectedLayer}
        setItemPosition={this.props.setItemPosition}
        setLayerStyle={this.props.setLayerStyle}
        toggleLayerConfiguration={this.props.toggleLayerConfiguration}
      />
    );
  }
}

const mapStateToProps = (state) => ({
  baseLayer: state.ui.views.map.config.baseLayer,
  bingMaps: state.config.bingMaps,
  defaultCenter: state.config.mapDefaults.center,
  draggable: state.ui.views.map.config.draggable,
  initialCenter: state.ui.views.map.config.center,
  initialZoom: state.ui.views.map.config.zoom,
  layerConfigVisible: state.ui.views.map.config.layerConfigVisible,
  layers: state.ui.views.map.config.layers,
  osm: state.config.osm,
  provenance: state.ui.views.map.config.provenance,
  resource: state.ui.views.map.data.resource,
  selectedFeature: state.ui.views.map.config.selectedFeature,
  selectedFeatures: state.ui.views.map.config.selectedFeatures,
  selectedLayer: state.ui.views.map.config.selectedLayer,
  version: state.ui.views.map.data.version,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  fetchFeatureProvenance,
  fetchResourceMapData,
  reset,
  selectFeatures,
  setCenter,
  setItemPosition,
  setLayerStyle,
  toggleLayerConfiguration,
}, dispatch);

const mergeProps = (stateProps, dispatchProps, ownProps) => {
  return {
    ...stateProps,
    ...dispatchProps,
    ...ownProps,
  };
};

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps, mergeProps)(ResourceMapViewer);
