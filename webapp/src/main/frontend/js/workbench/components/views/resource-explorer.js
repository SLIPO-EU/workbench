import * as React from 'react';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';
import {
  Card, CardBlock, CardTitle, Row, Col,
  ButtonToolbar, Button, ButtonGroup, Label, Input
} from 'reactstrap';
import moment from 'moment';
import { FormattedTime } from 'react-intl';

import Placeholder from './placeholder';
import { Filters, ResourceDetails, Resources } from './resource/explorer/';

import { fetchResources } from '../../ducks/data/resources';
import { setPager, resetPager, setFilter, resetFilters, setSelectedResource } from '../../ducks/ui/views/resource-explorer';


/**
 * Browse and manage resources
 *
 * @class ResourceExplorer
 * @extends {React.Component}
 */
class ResourceExplorer extends React.Component {

  componentWillMount() {
    this.props.fetchResources({});
  }

  render() {
    const { resources } = this.props;
    return (
      <div className="animated fadeIn">
        <Row>
          <Col className="col-12">
            <Card>
              <CardBlock className="card-body">
                <Row className="mb-2">
                  <Col >
                    <div className="small text-muted">Last Update: <FormattedTime value={moment().toDate()} day='numeric' month='numeric' year='numeric' /></div>
                  </Col>
                </Row>
                <Row style={{ height: 100 }} className="mb-2">
                  <Col>
                    <Filters 
                      filters={this.props.filters}
                      setFilter={this.props.setFilter}
                      resetFilters={this.props.resetFilters}
                      fetchResources={this.props.fetchResources}
                    />
                  </Col>
                </Row>
                <Row style={{ minHeight: 450 }} className="mb-2">
                  <Col>
                    <Resources 
                      resources={this.props.resources}
                      pager={this.props.pager}
                      setPager={this.props.setPager}
                      resetPager={this.props.resetPager}
                      fetchResources={this.props.fetchResources}
                      setSelectedResource={this.props.setSelectedResource}
                      selectedResource={this.props.selectedResource}
                    />
                  </Col>
                  <Col>
                    <Placeholder label="Map" iconClass="fa fa-map-o" />
                  </Col>
                </Row>
                <Row style={{ minHeight: 450, marginTop: 30 }} className="mb-2">
                  <Col>
                    <h3>Details</h3>
                    <ResourceDetails
                      resources={this.props.resources.items}
                      detailed={this.props.selectedResource}
                    />
                  </Col>
                </Row>
              </CardBlock>
            </Card>
          </Col>
        </Row >
      </div>
    );
  }

}

const mapStateToProps = (state) => ({
  resources: state.data.resources,
  pager: state.ui.views.resources.explorer.pager,
  filters: state.ui.views.resources.explorer.filters,
  selectedResource: state.ui.views.resources.explorer.selected,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({ fetchResources, setFilter, resetFilters, setPager, resetPager, setSelectedResource }, dispatch);

const mergeProps = (stateProps, dispatchProps, ownProps) => {
  return {
    ...stateProps,
    ...dispatchProps,
    ...ownProps,
  };
};

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps, mergeProps)(ResourceExplorer);
