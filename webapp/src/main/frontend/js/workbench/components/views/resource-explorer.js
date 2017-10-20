import * as React from 'react';
import * as ReactRedux from 'react-redux';
import { FormattedTime } from 'react-intl';
import { bindActionCreators } from 'redux';
import {
  Card, CardBlock, CardTitle, Row, Col,
  ButtonToolbar, Button, ButtonGroup, Label, Input
} from 'reactstrap';

import Placeholder from './placeholder';

import moment from 'moment';

import { fetchResources } from '../../ducks/data/resources';

/**
 * Browse and manage resources
 *
 * @class ResourceExplorer
 * @extends {React.Component}
 */
class ResourceExplorer extends React.Component {

  componentWillMount() {
    this.props.fetchResources();
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
                <Row style={{ height: 200 }} className="mb-2">
                  <Col>
                    <Placeholder label="Filter" iconClass="fa fa-filter" />
                  </Col>
                </Row>
                <Row style={{ height: 400 }} className="mb-2">
                  <Col>
                    <Placeholder label="Resources" iconClass="fa fa-table" />
                  </Col>
                  <Col>
                    <Placeholder label="Map" iconClass="fa fa-map-o" />
                  </Col>
                </Row>
                <Row style={{ height: 400 }} className="mb-2">
                  <Col>
                    <Placeholder label="Details" iconClass="fa fa-database" />
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
});

const mapDispatchToProps = (dispatch) => bindActionCreators({ fetchResources }, dispatch);

const mergeProps = (stateProps, dispatchProps, ownProps) => {
  return {
    ...stateProps,
    ...dispatchProps,
    ...ownProps,
  };
};

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps, mergeProps)(ResourceExplorer);
