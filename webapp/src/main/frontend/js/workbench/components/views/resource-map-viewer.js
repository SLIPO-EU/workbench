import * as React from 'react';
import * as ReactRedux from 'react-redux';

import {
  bindActionCreators
} from 'redux';

import {
  bringToFront,
  clearSelectedFeatures,
  fetchFeatureProvenance,
  fetchResourceMapData,
  hideProvenance,
  reset,
  setFilter,
  toggleFilter,
  selectFeatures,
  selectGeometrySnapshot,
  setCenter,
  setItemPosition,
  setLayerStyle,
  toggleEditor,
  toggleLayerConfiguration,
  updateFeatureVertex,
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
        bringToFront={this.props.bringToFront}
        clearSelectedFeatures={this.props.clearSelectedFeatures}
        defaultCenter={defaultCenter}
        draggable={this.props.draggable}
        draggableOrder={this.props.draggableOrder}
        editActive={this.props.editActive}
        editFeature={this.props.editFeature}
        fetchFeatureProvenance={
          (feature) => fetchFeatureProvenance(
            execution.id,
            execution.version,
            execution.execution,
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
        onGeometryChange={() => this.props.updateFeatureVertex()}
        onGeometrySnapshotChange={(index, geometry) => this.props.selectGeometrySnapshot(index, geometry)}
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
  filterFormVisible: state.ui.views.map.search.visible,
  filters: state.ui.views.map.search.filters,
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
  viewport: state.ui.viewport,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  bringToFront,
  clearSelectedFeatures,
  fetchFeatureProvenance,
  fetchResourceMapData,
  hideProvenance,
  reset,
  setFilter,
  toggleFilter,
  selectFeatures,
  selectGeometrySnapshot,
  setCenter,
  setItemPosition,
  setLayerStyle,
  toggleEditor,
  toggleLayerConfiguration,
  updateFeatureVertex,
}, dispatch);

const mergeProps = (stateProps, dispatchProps, ownProps) => {
  return {
    ...stateProps,
    ...dispatchProps,
    ...ownProps,
  };
};

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps, mergeProps)(ResourceMapViewer);
