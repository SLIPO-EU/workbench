import * as React from 'react';
import * as ReactRedux from 'react-redux';

import {
  bindActionCreators
} from 'redux';

import {
  bringToFront,
  clearSelectedFeatures,
  fetchFeatureProvenance,
  fetchExecutionMapData,
  hideProvenance,
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
 * Renders a map from the input/output datasets of a single process
 * execution instance
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
    this.onMoveEnd = this.onMoveEnd.bind(this);
  }

  get params() {
    const { id, version, execution } = this.props.match.params;

    return {
      processId: Number.parseInt(id),
      processVersion: Number.parseInt(version),
      executionId: Number.parseInt(execution),
    };
  }

  componentDidMount() {
    const params = this.params;
    const { execution } = this.props;

    if ((!execution) || (execution.id !== params.executionId)) {
      this.props.reset();
      this.props.fetchExecutionMapData(params.processId, params.processVersion, params.executionId)
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
      execution,
      fetchFeatureProvenance,
      layers,
      osm,
    } = this.props;

    return (
      <MapViewer
        baseLayer={baseLayer}
        bingMaps={bingMaps}
        bringToFront={this.props.bringToFront}
        clearSelectedFeatures={this.props.clearSelectedFeatures}
        defaultCenter={defaultCenter}
        draggable={this.props.draggable}
        draggableOrder={this.props.draggableOrder}
        fetchFeatureProvenance={
          (outputKey, featureId, featureUri) => fetchFeatureProvenance(
            execution.process.id,
            execution.process.version,
            execution.id,
            outputKey,
            featureId,
            featureUri)
        }
        hideProvenance={this.props.hideProvenance}
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
  draggableOrder: state.ui.views.map.config.draggableOrder,
  execution: state.ui.views.map.data.execution,
  initialCenter: state.ui.views.map.config.center,
  initialZoom: state.ui.views.map.config.zoom,
  layerConfigVisible: state.ui.views.map.config.layerConfigVisible,
  layers: state.ui.views.map.config.layers,
  osm: state.config.osm,
  provenance: state.ui.views.map.config.provenance,
  selectedFeature: state.ui.views.map.config.selectedFeature,
  selectedFeatures: state.ui.views.map.config.selectedFeatures,
  selectedLayer: state.ui.views.map.config.selectedLayer,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  bringToFront,
  clearSelectedFeatures,
  fetchFeatureProvenance,
  fetchExecutionMapData,
  hideProvenance,
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

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps, mergeProps)(ProcessExecutionMapViewer);
