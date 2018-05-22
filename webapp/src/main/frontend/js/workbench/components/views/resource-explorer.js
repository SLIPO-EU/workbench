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
  toast
} from 'react-toastify';


import {
  OpenLayers,
  ToastTemplate,
} from '../helpers';

import {
  Filters,
  Resources,
  ResourceDetails,
} from './resource/explorer/';

import {
  fetchResources,
  resetFilters,
  setFilter,
  setPager,
  setSelectedResource,
} from '../../ducks/ui/views/resource-explorer';

import {
  addResourceToBag,
  removeResourceFromBag,
} from '../../ducks/ui/views/process-designer';

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
    this.onFeatureSelect = this.onFeatureSelect.bind(this);
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
    toast.dismiss();

    toast.error(
      <ToastTemplate iconClass='fa-warning' text='Not implemented!' />
    );
  }

  render() {
    return (
      <div className="animated fadeIn">
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
                  {this.props.selected &&
                    <Col xs={6}>
                      <ResourceDetails
                        resource={this.props.items.find((r) => this.props.selected && r.id === this.props.selected.id)}
                        version={this.props.selected ? this.props.selected.version : null}
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
  fetchResources,
  removeResourceFromBag,
  resetFilters,
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
