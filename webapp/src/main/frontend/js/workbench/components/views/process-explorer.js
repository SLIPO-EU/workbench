import * as React from 'react';
import * as ReactRedux from 'react-redux';
import { FormattedTime } from 'react-intl';
import { bindActionCreators } from 'redux';
import {
  Card, CardBlock, CardTitle, Row, Col,
  ButtonToolbar, Button, ButtonGroup, Label, Input
} from 'reactstrap';

import Placeholder from './placeholder';
import { Filters } from './resource/explorer/'; // STC


// Import React Table
import ReactTable from "react-table";
import { Processes , ProcessExecutions , ExecutionDetails } from "./process";


import moment from 'moment';
import { fetchProcessData, setPager, setSelectedProcess, setSelectedExecution } from '../../ducks/ui/views/process-explorer';

/**
 * Component for managing job scheduling
 *
 * @class ProcessExplorer
 * @extends {React.Component}
 */
class ProcessExplorer extends React.Component {
  componentWillMount(){
    this.props.fetchProcessData(); 
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
                      filters=''//{this.props.filters}
                      setFilter=''//{this.props.setFilter}
                      resetFilters=''//{this.props.resetFilters}
                      fetchResources=''//{this.props.fetchResources}
                    />
                  </Col>
                </Row>
                <Row style={{ height: 400, marginBottom: 20}} className="mb-2">
                  <Col>
                    <Processes
                      processes={this.props.processes}
                      fetchProcessData={this.props.fetchProcessData}
                      setPager={this.props.setPager}
                      setSelectedProcess={this.props.setSelectedProcess}
                      selectedProcess={this.props.processes.selected}
                    />
                  </Col>
                </Row>
                <Row style={{ minHeight: 450, marginTop: 40 }} className="mb-2">
                  <Col>
                    <h3>Executions</h3>
                    <ProcessExecutions
                      processes={this.props.processes.items}
                      detailed={this.props.processes.selected}
                      selectedFields={this.props.selectedFields}
                      setSelectedExecution= {this.props.setSelectedExecution}
                      selectedExecution={this.props.processes.selectedExecution}
                      selectedProcess={this.props.processes.selected}
                    />
                  </Col>
                  <Col xs="3">
                    <h3>Details</h3>
                    <ExecutionDetails 
                      steps= {this.props.processes.executionStatus.steps} />
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
  processes: state.ui.views.process.explorer,
  selectedFields: state.ui.views.process.explorer.selectedFields,
  //pager: state.ui.views.processes.pagingOptions,
  //resources: state.ui.views.dashboard.resources,
  //events: state.ui.views.dashboard.events,
  //filters: state.ui.views.dashboard.filters,
  //givenName: state.user.profile.givenName,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({ fetchProcessData, setPager, setSelectedProcess, setSelectedExecution }, dispatch);

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps)(ProcessExplorer);
