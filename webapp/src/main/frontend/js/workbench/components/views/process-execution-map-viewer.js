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
  Button,
  Card,
  CardBody,
  Col,
  Row,
} from 'reactstrap';

import {
  OpenLayers,
  ToastTemplate,
} from '../../components/helpers';

import {
  buildPath,
  DynamicRoutes,
} from '../../model';

import {
  fetchExecutionDetails,
  reset,
  setBaseLayer,
  selectFeatures,
} from '../../ducks/ui/views/process-designer';

import {
  SelectField,
} from '../helpers/forms/fields';

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
    this.viewExecution = this.viewExecution.bind(this);
  }

  get supportedBaseLayers() {
    const baseLayers = [
      { value: 'OSM', label: 'Open Street Maps' },
    ];

    if (this.props.bingMaps.applicationKey) {
      baseLayers.push({ value: 'BingMaps', label: 'Bing Maps' });
    }

    return baseLayers;
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

  viewExecution() {
    const { id, version, execution, ...rest } = this.props.match.params;
    const path = buildPath(DynamicRoutes.ProcessExecutionViewer, [id, version, execution]);

    this.props.history.push(path);
  }

  error(message, goBack) {
    toast.dismiss();

    toast.error(
      <ToastTemplate iconClass='fa-warning' text={message} />
    );

    if ((typeof goBack === 'undefined') || (goBack)) {
      setTimeout(() => this.props.history.goBack(), 500);
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
      case 'BingMaps':
        if (this.props.bingMaps.applicationKey) {
          layers.push(
            <OpenLayers.Layer.BingMaps
              key="bing-maps"
              applicationKey={this.props.bingMaps.applicationKey}
              imagerySet={this.props.bingMaps.imagerySet}
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
            key={l.tableName}
            url="/action/proxy/service/wfs"
            version="1.1.0"
            typename={`slipo_eu:${l.tableName}`}
            icon={l.icon}
            color={l.color}
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
        <Card>
          <CardBody>
            <Row>
              <Col xs={6} md={3}>
                <SelectField
                  id="baseLayer"
                  label="Base Layer"
                  value={this.props.baseLayer || 'OSM'}
                  onChange={(value) => this.props.setBaseLayer(value)}
                  options={this.supportedBaseLayers}
                />
              </Col>
              <Col className="pt-1">
                <Button color="primary" onClick={this.viewExecution} className="float-right"><i className="fa fa-cog"></i> View Execution</Button>
              </Col>
            </Row>
            <Row>
              <Col>
                <OpenLayers.Map minZoom={12} maxZoom={18} zoom={15} center={this.center}>
                  <OpenLayers.Layers>
                    {this.getLayers()}
                  </OpenLayers.Layers>
                  <OpenLayers.Interactions>
                    <OpenLayers.Interaction.Select
                      onFeatureSelect={this.onFeatureSelect}
                      icon={'\uf21d'}
                    />
                  </OpenLayers.Interactions>
                </OpenLayers.Map>
                <div className="small text-muted">Hold Shift to select multiple features</div>
              </Col>
            </Row>
          </CardBody>
        </Card>
      </div>
    );
  }
}

const mapStateToProps = (state) => ({
  // Configuration
  osm: state.config.osm,
  bingMaps: state.config.bingMaps,
  layers: state.ui.views.process.designer.execution.layers,
  baseLayer: state.ui.views.process.designer.execution.baseLayer,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  fetchExecutionDetails,
  setBaseLayer,
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
