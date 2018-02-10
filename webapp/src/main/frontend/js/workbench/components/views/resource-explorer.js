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

import OpenLayers from '../helpers/map';

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

    this.onFeatureSelect = this.onFeatureSelect.bind(this);
  }

  /**
   * Initializes a request for fetching resource data, optionally using any
   * existing search criteria
   *
   * @memberof ResourceExplorer
   */
  componentWillMount() {
    this.props.fetchResources({
      query: {...this.props.filters},
    });
  }

  /**
   * Syncs map component to table selected row
   *
   * @param {any} e
   * @memberof ResourceExplorer
   */
  onFeatureSelect(e) {
    if (e.selected.length === 1) {
      const feature = e.selected[0];
      this.props.setSelectedResource(feature.get('id'), feature.get('version'));
    }
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
                  <Col xs="6">
                    <Resources
                      addResourceToBag={this.props.addResourceToBag}
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
                  <Col xs="6">
                    <OpenLayers.Map minZoom={5} maxZoom={15} zoom={9}>
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
                        />
                      </OpenLayers.Interactions>
                    </OpenLayers.Map>
                  </Col>
                </Row>
              </CardBody>
            </Card>
            {this.props.selected &&
              <Card>
                <CardBody className="card-body">
                  <Row>
                    <Col>
                      <ResourceDetails
                        resource={this.props.items.find((r) => this.props.selected && r.id === this.props.selected.id)}
                        version={this.props.selected ? this.props.selected.version : null}
                      />
                    </Col>
                  </Row>
                </CardBody>
              </Card>
            }
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
