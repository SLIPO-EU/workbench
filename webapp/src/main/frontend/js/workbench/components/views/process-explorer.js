import * as React from 'react';
import * as ReactRedux from 'react-redux';

import {
  bindActionCreators
} from 'redux';

import {
  toast
} from 'react-toastify';

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
  buildPath
} from '../../model/routes';

import {
  ToastTemplate,
} from '../helpers';

import {
  ExecutionDetails,
  Filters,
  Processes,
  ProcessExecutions,
} from "./process/explorer";


import {
  fetchProcessExecutions,
  fetchProcesses,
  resetFilters,
  setFilter,
  setPager,
} from '../../ducks/ui/views/process-explorer';

/**
 * Browse and manage processes
 *
 * @class ProcessExplorer
 * @extends {React.Component}
 */
class ProcessExplorer extends React.Component {

  constructor(props) {
    super(props);

    this.editProcess = this.editProcess.bind(this);
    this.viewProcess = this.viewProcess.bind(this);
    this.viewExecution = this.viewExecution.bind(this);
    this.viewMap = this.viewMap.bind(this);
    this.startExecution = this.startExecution.bind(this);
    this.stopExecution = this.stopExecution.bind(this);
  }

  /**
   * Initializes a request for fetching process data, optionally using any
   * existing search criteria
   *
   * @memberof ProcessExplorer
   */
  componentWillMount() {
    this.props.fetchProcesses({
      query: { ...this.props.filters },
    });
  }

  /**
   * Navigates to the {@link ProcessDesigner} component for editing the current
   * version of the selected process
   *
   * @param {*} id
   */
  editProcess(id) {
    const path = buildPath(DynamicRoutes.ProcessDesignerEdit, [id]);

    this.props.history.push(path);
  }

  /**
   * Navigates to the {@link ProcessDesigner} component for viewing the specific
   * version of the selected process in read-only mode
   *
   * @param {any} id
   * @param {any} version
   * @memberof ProcessExplorer
   */
  viewProcess(id, version) {
    const path = buildPath(DynamicRoutes.ProcessDesignerView, [id, version]);

    this.props.history.push(path);
  }

  /**
   * Displays details about the selected execution
   *
   * @param {any} id
   * @param {any} version
   * @param {any} execution
   * @memberof ProcessExplorer
   */
  viewExecution(id, version, execution) {
    const path = buildPath(DynamicRoutes.ProcessExecutionViewer, [id, version, execution]);

    this.props.history.push(path);
  }

  /**
   * Render a map with input/output data
   *
   * @param {any} id
   * @param {any} version
   * @param {any} execution
   * @memberof ProcessExplorer
   */
  viewMap(id, version, execution) {
    const path = buildPath(DynamicRoutes.ProcessExecutionMapViewer, [id, version, execution]);

    this.props.history.push(path);
  }

  /**
   * Starts the execution of the current version of the selected process
   *
   * @param {any} id
   * @memberof ProcessExplorer
   */
  startExecution(id) {
    toast.dismiss();

    toast.error(
      <ToastTemplate iconClass='fa-warning' text='Not implemented!' />
    );
  }

  /**
   * Attempts to stop the selected execution
   *
   * @param {any} id
   * @memberof ProcessExplorer
   */
  stopExecution(id) {
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
                      fetchProcesses={this.props.fetchProcesses}
                    />
                  </Col>
                </Row>
              </CardBody>
            </Card>
            <Card>
              <CardBody className="card-body">
                <Row className="mb-2">
                  <Col>
                    <Processes
                      editProcess={this.editProcess}
                      viewProcess={this.viewProcess}
                      fetchProcessExecutions={this.props.fetchProcessExecutions}
                      fetchProcesses={this.props.fetchProcesses}
                      filters={this.props.filters}
                      items={this.props.items}
                      pager={this.props.pager}
                      selected={this.props.selected}
                      setPager={this.props.setPager}
                      startExecution={this.startExecution}
                    />
                  </Col>
                </Row>
              </CardBody>
            </Card>
            {this.props.selected &&
              <Card>
                <CardBody className="card-body">
                  <Row>
                    <Col>
                      <ProcessExecutions
                        executions={this.props.executions}
                        selected={this.props.selected}
                        stopExecution={this.stopExecution}
                        viewExecution={this.viewExecution}
                        viewMap={this.viewMap}
                      />
                    </Col>
                  </Row>
                </CardBody>
              </Card>
            }
          </Col>
        </Row >
      </div>
    );
  }

}

const mapStateToProps = (state) => ({
  executions: state.ui.views.process.explorer.executions,
  filters: state.ui.views.process.explorer.filters,
  items: state.ui.views.process.explorer.items,
  lastUpdate: state.ui.views.process.explorer.lastUpdate,
  pager: state.ui.views.process.explorer.pager,
  selected: state.ui.views.process.explorer.selected,
  selectedExecution: state.ui.views.process.explorer.selectedExecution,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  fetchProcesses,
  setFilter,
  resetFilters,
  setPager,
  fetchProcessExecutions,
}, dispatch);

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps)(ProcessExplorer);
