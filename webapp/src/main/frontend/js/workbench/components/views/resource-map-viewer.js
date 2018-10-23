import * as React from 'react';
import * as ReactRedux from 'react-redux';

import {
  bindActionCreators
} from 'redux';

import {
  fetchResourceMapData,
  reset,
  selectFeatures,
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
  }

  render() {
    const { baseLayer, bingMaps, defaultCenter, layers, osm } = this.props;

    if (this.state.isLoading) {
      return null;
    }
    return (
      <div className="animated fadeIn">
        <MapViewer
          baseLayer={baseLayer}
          bingMaps={bingMaps}
          defaultCenter={defaultCenter}
          layers={layers}
          osm={osm}
          onFeatureSelect={(features) => this.props.selectFeatures(features)}
          onMoveEnd={this.onMoveEnd}
          selectedFeatures={this.props.selectedFeatures}
        />
      </div >
    );
  }
}

const mapStateToProps = (state) => ({
  baseLayer: state.ui.views.map.config.baseLayer,
  bingMaps: state.config.bingMaps,
  defaultCenter: state.config.mapDefaults.center,
  layers: state.ui.views.map.config.layers,
  osm: state.config.osm,
  resource: state.ui.views.map.data.resource,
  selectedFeatures: state.ui.views.map.config.selectedFeatures,
  version: state.ui.views.map.data.version,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  fetchResourceMapData,
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

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps, mergeProps)(ResourceMapViewer);
