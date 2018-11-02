import * as React from 'react';
import * as ReactRedux from 'react-redux';

import {
  bindActionCreators
} from 'redux';

import {
  fetchExecutionMapData,
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
  execution: state.ui.views.map.data.execution,
  layers: state.ui.views.map.config.layers,
  osm: state.config.osm,
  selectedFeatures: state.ui.views.map.config.selectedFeatures,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  fetchExecutionMapData,
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
