import * as React from 'react';
import * as ReactRedux from 'react-redux';

import {
  bindActionCreators
} from 'redux';

import {
  Card,
  CardBody,
  Col,
  Row,
} from 'reactstrap';

import {
  FormattedTime
} from 'react-intl';

import {
  DynamicRoutes,
  buildPath,
  StaticRoutes,
} from '../../model';

import {
  OpenLayers,
} from '../helpers';

import {
  Filters,
  Resources,
  ResourceDetails,
} from './resource/explorer/';

import {
  search as fetchResources,
  resetFilters,
  setFilter,
  setPager,
  setSelectedResource,
} from '../../ducks/ui/views/resource-explorer';

import {
  setTemp as setExportResource,
} from '../../ducks/ui/views/resource-export';

import {
  addResourceToBag,
  removeResourceFromBag,
} from '../../ducks/ui/views/process-designer';

import {
  exportMap,
} from '../../ducks/ui/views/process-explorer';

import {
  message,
} from '../../service';

/**
 * Browse and manage resources
 *
 * @class ResourceExplorer
 * @extends {React.Component}
 */
class ResourceExplorer extends React.Component {

  constructor(props) {
    super(props);

    this.deleteResource = this.deleteResource.bind(this);
    this.exportMap = this.exportMap.bind(this);
    this.exportResource = this.exportResource.bind(this);
    this.onFeatureSelect = this.onFeatureSelect.bind(this);
    this.viewMap = this.viewMap.bind(this);
  }

  /**
   * Initializes a request for fetching resource data, optionally using any
   * existing search criteria
   *
   * @memberof ResourceExplorer
   */
  componentWillMount() {
    if (!this.props.items.length) {
      this.props.fetchResources({
        query: { ...this.props.filters },
      });
    }
  }

  /**
   * Syncs map component to table selected row
   *
   * @param {any} features
   * @memberof ResourceExplorer
   */
  onFeatureSelect(features) {
    if (features.length === 1) {
      const feature = features[0];
      this.props.setSelectedResource(feature.get('id'), feature.get('version'));
    }
  }

  /**
   * Deletes an existing resource
   *
   * @param {any} id
   * @param {any} version
   * @memberof ResourceExplorer
   */
  deleteResource(id, version) {
    message.error('error.NOT_IMPLEMENTED', 'fa-warning');
  }

  /**
   * Exports the selected resource to a file using TripleGeo
   * reverse transformation
   *
   * @param {*} resource
   */
  exportResource(resource) {
    this.props.setExportResource(null, {
      catalog: {
        resource,
      }
    });
    this.props.history.push(StaticRoutes.ResourceExport);
  }

  /**
   * Exports process execution data to a relation database
   * for rendering a map with input/output datasets as layers
   *
   * @param {*} id
   * @param {*} version
   * @param {*} executionId
   * @memberof ResourceExplorer
   */
  exportMap(id, version, executionId) {
    this.props.exportMap(id, version, executionId)
      .then(() => {
        message.info('Process execution export has started successfully');
      }).catch((err) => {
        message.error(err.message);
      });
  }

  /**
   * Renders a map with the input/output datasets of the process
   * execution instance that created the specific resource version
   *
   * @param {*} id
   * @param {*} version
   * @param {*} execution
   * @memberof ResourceExplorer
   */
  viewMap(id, version, execution) {
    const path = buildPath(DynamicRoutes.ProcessExecutionMapViewer, [id, version, execution]);

    this.props.history.push(path);
  }

  render() {
    const { items, selected: selection } = this.props;

    let resource = null;
    if (selection) {
      resource = items.find((r) => r.id === selection.id);
      resource = resource ? resource.revisions.find((r) => r.version === selection.version) : null;
    }

    return (
      <div className="animated fadeIn" >
        <Row>
          <Col className="col-12">
            <Card>
              <CardBody className="card-body">
                {this.props.lastUpdate &&
                  <Row className="mb-2">
                    <Col >
                      <div className="small text-muted">
                        Last Update: <FormattedTime value={this.props.lastUpdate} day='numeric' month='numeric' year='numeric' />
                      </div>
                    </Col>
                  </Row>
                }
                <Row>
                  <Col>
                    <Filters
                      filters={this.props.filters}
                      setFilter={this.props.setFilter}
                      resetFilters={this.props.resetFilters}
                      fetchResources={this.props.fetchResources}
                    />
                  </Col>
                </Row>
              </CardBody>
            </Card>
            <Card>
              <CardBody className="card-body">
                <Row className="mb-2">
                  <Col>
                    <Resources
                      addResourceToBag={this.props.addResourceToBag}
                      deleteResource={this.deleteResource}
                      exportMap={this.exportMap}
                      exportResource={this.exportResource}
                      fetchResources={this.props.fetchResources}
                      filters={this.props.filters}
                      items={this.props.items}
                      pager={this.props.pager}
                      removeResourceFromBag={this.props.removeResourceFromBag}
                      selected={this.props.selected}
                      selectedResources={this.props.selectedResources}
                      setPager={this.props.setPager}
                      setSelectedResource={this.props.setSelectedResource}
                    />
                  </Col>
                </Row>
              </CardBody>
            </Card>
            <Card>
              <CardBody className="card-body">
                <Row>
                  <Col xs="6" className="slipo-map-container">
                    <OpenLayers.Map minZoom={5} maxZoom={11} zoom={8}>
                      <OpenLayers.Layers>
                        <OpenLayers.Layer.OSM
                          url="http://tile.stamen.com/terrain/{z}/{x}/{y}.jpg"
                        />
                        <OpenLayers.Layer.GeoJSON
                          features={this.props.features}
                        />
                      </OpenLayers.Layers>
                      <OpenLayers.Interactions>
                        <OpenLayers.Interaction.Select
                          onFeatureSelect={this.onFeatureSelect}
                          selected={this.props.selectedFeatures}
                          color={'#0D47A1'}
                          multi={false}
                          width={1}
                        />
                      </OpenLayers.Interactions>
                    </OpenLayers.Map>
                  </Col>
                  {!this.props.selected &&
                    <span className="text-muted">Select a resource to view details ...</span>
                  }
                  {resource &&
                    <Col xs={6}>
                      <ResourceDetails
                        resource={resource}
                        viewMap={this.viewMap}
                      />
                    </Col>
                  }
                </Row>
              </CardBody>
            </Card>
          </Col>
        </Row >
      </div >
    );
  }
}

const mapStateToProps = (state) => ({
  features: state.ui.views.resources.explorer.features,
  filters: state.ui.views.resources.explorer.filters,
  items: state.ui.views.resources.explorer.items,
  lastUpdate: state.ui.views.resources.explorer.lastUpdate,
  pager: state.ui.views.resources.explorer.pager,
  selected: state.ui.views.resources.explorer.selected,
  selectedFeatures: state.ui.views.resources.explorer.selectedFeatures,
  selectedResources: state.ui.views.process.designer.resources,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  addResourceToBag,
  exportMap,
  fetchResources,
  removeResourceFromBag,
  resetFilters,
  setExportResource,
  setFilter,
  setPager,
  setSelectedResource,
}, dispatch);

const mergeProps = (stateProps, dispatchProps, ownProps) => {
  return {
    ...stateProps,
    ...dispatchProps,
    ...ownProps,
  };
};

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps, mergeProps)(ResourceExplorer);
