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

/**
 * Component for managing job scheduling
 *
 * @class ProcessExplorer
 * @extends {React.Component}
 */
class ProcessExplorer extends React.Component {

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
                    <Placeholder label="Filter" iconClass="fa fa-flter" />
                  </Col>
                </Row>
                <Row style={{ height: 400 }} className="mb-2">
                  <Col>
                    <Placeholder label="Processes" iconClass="fa fa-table" />
                  </Col>
                </Row>
                <Row style={{ height: 400 }} className="mb-2">
                  <Col>
                    <Placeholder label="Executions" iconClass="fa fa-table" />
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

export default ProcessExplorer;
