import * as React from 'react';
import * as ReactRedux from 'react-redux';

import {
  bindActionCreators
} from 'redux';

import {
  findOne as findResource,
} from '../../ducks/ui/views/resource-explorer';

import {
  Card,
  CardBody,
  Col,
  Row,
} from 'reactstrap';

import {
  DynamicRoutes,
  buildPath,
  StaticRoutes,
} from '../../model';

import {
  OpenLayers,
} from '../helpers';

import {
  ResourceDetails,
} from './resource/explorer/';

import {
  message,
} from '../../service';

/**
 * Resource metadata viewer
 *
 * @class ResourceViewer
 * @extends {React.Component}
 */
class ResourceViewer extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      isLoading: true,
    };

    this.onFetchError = this.onFetchError.bind(this);
    this.onFetchSuccess = this.onFetchSuccess.bind(this);
    this.onFeatureSelect = this.onFeatureSelect.bind(this);
    this.viewMap = this.viewMap.bind(this);
  }

  componentDidMount() {
    const { id, version } = this.props.match.params;

    this.props.findResource(Number.parseInt(id), Number.parseInt(version))
      .then(this.onFetchSuccess)
      .catch(this.onFetchError);
  }

  onFetchSuccess() {
    this.setState({ isLoading: false });
  }

  onFetchError(err) {
    message.error(err.message, 'fa-warning');

    setTimeout(() => {
      this.props.history.push(StaticRoutes.ResourceExplorer);
    }, 500);
  }

  onFeatureSelect(features) {
    console.log(features);
  }

  viewMap(id, version) {
    const path = buildPath(DynamicRoutes.ResourceMapViewer, [id, version]);

    this.props.history.push(path);
  }

  render() {
    const { resource } = this.props;
    const icon = '\uf08d';
    const color = '#B80000';

    return (
      <div className="animated fadeIn">
        {resource &&
          <Row>
            <Col className="col-12">
              <Card>
                <CardBody className="card-body">
                  <ResourceDetails
                    resource={resource}
                    viewMap={this.viewMap}
                  />
                </CardBody>
              </Card>
            </Col>
          </Row>
        }
        {resource && resource.tableName &&
          <Row>
            <Col>
              <OpenLayers.Map minZoom={13} maxZoom={18} zoom={15} center={[1545862.48, 6026906.87]}>
                <OpenLayers.Layers>
                  <OpenLayers.Layer.BingMaps
                    key="bing-maps-road"
                    applicationKey={this.props.bingMaps.applicationKey}
                    imagerySet="Road"
                  />
                  <OpenLayers.Layer.WFS
                    key={`${resource.tableName}-${color}-${icon}`}
                    url="/action/proxy/service/wfs"
                    version="1.1.0"
                    typename={`slipo_eu:${resource.tableName}`}
                    color={color}
                    icon={icon}
                  />
                </OpenLayers.Layers>
                <OpenLayers.Interactions>
                  <OpenLayers.Interaction.Select
                    onFeatureSelect={this.onFeatureSelect}
                    multi={true}
                    width={2}
                  />

                </OpenLayers.Interactions>
              </OpenLayers.Map>
            </Col>
          </Row>
        }
      </div>
    );
  }

}

const mapStateToProps = (state) => ({
  resource: state.ui.views.resources.explorer.resource,
  bingMaps: state.config.bingMaps,
  osm: state.config.osm,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  findResource,
}, dispatch);

const mergeProps = (stateProps, dispatchProps, ownProps) => {
  return {
    ...stateProps,
    ...dispatchProps,
    ...ownProps,
  };
};

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps, mergeProps)(ResourceViewer);
