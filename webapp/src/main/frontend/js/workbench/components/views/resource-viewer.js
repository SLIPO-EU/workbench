import * as React from 'react';
import * as ReactRedux from 'react-redux';

import GeoJSON from 'ol/format/GeoJSON';

import { getCenter as getExtentCenter } from 'ol/extent';

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

import {
  Colors,
} from '../../model/constants';

import {
  EnumSymbol,
} from '../../model/map-viewer';

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

  viewMap(id, version) {
    const path = buildPath(DynamicRoutes.ResourceMapViewer, [id, version]);

    this.props.history.push(path);
  }

  getLayerStyle() {
    return {
      symbol: EnumSymbol.Square,
      fill: {
        color: Colors[0],
      },
      stroke: {
        color: Colors[0],
        width: 2,
      },
      size: 20,
      opacity: 50,
    };
  }

  getCenter() {
    const { defaultCenter, resource: { boundingBox } } = this.props;

    if (boundingBox) {
      const format = new GeoJSON();
      const geometry = format.readGeometry(boundingBox, {
        dataProjection: 'EPSG:4326',
        featureProjection: 'EPSG:3857',
      });
      const extent = geometry.getExtent();
      const center = getExtentCenter(extent);

      return center || defaultCenter;
    }
    return defaultCenter;
  }

  render() {
    const { resource } = this.props;

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
              <OpenLayers.Map minZoom={12} maxZoom={18} zoom={14} center={this.getCenter()}>
                <OpenLayers.Layers>
                  <OpenLayers.Layer.BingMaps
                    key="bing-maps-road"
                    applicationKey={this.props.bingMaps.applicationKey}
                    imagerySet="Road"
                  />
                  <OpenLayers.Layer.WFS
                    key={`${resource.tableName}`}
                    url="/action/proxy/service/wfs"
                    version="1.1.0"
                    typename={`slipo_eu:${resource.tableName}`}
                    style={resource.style || this.getLayerStyle()}
                  />
                </OpenLayers.Layers>
              </OpenLayers.Map>
            </Col>
          </Row>
        }
      </div>
    );
  }

}

const mapStateToProps = (state) => ({
  bingMaps: state.config.bingMaps,
  defaultCenter: state.config.mapDefaults.center,
  osm: state.config.osm,
  resource: state.ui.views.resources.explorer.resource,
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
