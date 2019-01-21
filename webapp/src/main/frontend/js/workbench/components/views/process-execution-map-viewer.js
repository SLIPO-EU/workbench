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
  setFilter,
  toggleFilter,
  selectFeatures,
  setCenter,
  setItemPosition,
  setLayerStyle,
  toggleEditor,
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
        editActive={this.props.editActive}
        editFeature={this.props.editFeature}
        fetchFeatureProvenance={
          (feature) => fetchFeatureProvenance(
            execution.process.id,
            execution.process.version,
            execution.id,
            feature)
        }
        filterFormVisible={this.props.filterFormVisible}
        filters={this.props.filters}
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
        setFilter={this.props.setFilter}
        setItemPosition={this.props.setItemPosition}
        setLayerStyle={this.props.setLayerStyle}
        toggleEditor={this.props.toggleEditor}
        toggleFilterForm={this.props.toggleFilter}
        toggleLayerConfiguration={this.props.toggleLayerConfiguration}
        viewport={this.props.viewport}
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
  editActive: state.ui.views.map.edit.active,
  editFeature: state.ui.views.map.edit.feature,
  execution: state.ui.views.map.data.execution,
  filterFormVisible: state.ui.views.map.search.visible,
  filters: state.ui.views.map.search.filters,
  initialCenter: state.ui.views.map.config.center,
  initialZoom: state.ui.views.map.config.zoom,
  layerConfigVisible: state.ui.views.map.config.layerConfigVisible,
  layers: state.ui.views.map.config.layers,
  osm: state.config.osm,
  provenance: state.ui.views.map.config.provenance,
  selectedFeature: state.ui.views.map.config.selectedFeature,
  selectedFeatures: state.ui.views.map.config.selectedFeatures,
  selectedLayer: state.ui.views.map.config.selectedLayer,
  viewport: state.ui.viewport,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  bringToFront,
  clearSelectedFeatures,
  fetchFeatureProvenance,
  fetchExecutionMapData,
  hideProvenance,
  reset,
  setFilter,
  toggleFilter,
  selectFeatures,
  setCenter,
  setItemPosition,
  setLayerStyle,
  toggleEditor,
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
