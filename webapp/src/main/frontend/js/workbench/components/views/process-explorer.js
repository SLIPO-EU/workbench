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
  EnumErrorLevel,
  UPDATE_INTERVAL_SECONDS,
} from '../../model';

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
  start,
  stop,
} from '../../ducks/ui/views/process-explorer';

// TODO: Add i18n support

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

    this.refreshIntervalId = null;
  }

  /**
   * Initializes a request for fetching process data, optionally using any
   * existing search criteria
   *
   * @memberof ProcessExplorer
   */
  componentWillMount() {
    this.refreshIntervalId = setInterval(() => {
      this.search();
    }, UPDATE_INTERVAL_SECONDS * 1000);

    this.search();
  }

  componentWillUnmount() {
    if (this.refreshIntervalId) {
      clearInterval(this.refreshIntervalId);
      this.refreshIntervalId = null;
    }
  }

  search() {
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
   * Starts the execution of the selected process revision
   *
   * @param {any} id
   * @param {any} version
   * @memberof ProcessExplorer
   */
  startExecution(id, version) {
    this.props.start(id, version)
      .then((data) => {
        this.displayMessage('Process execution has started successfully', EnumErrorLevel.INFO);
      })
      .catch((err) => {
        this.displayMessage(err.message);
      })
      .finally(() => {
        this.search();
      });
  }

  /**
   * Attempts to stop the execution of the selected process revision
   *
   * @param {any} id
   * @param {any} version
   * @memberof ProcessExplorer
   */
  stopExecution(id, version) {
    this.props.stop(id, version)
      .catch((err) => {
        this.displayMessage(err.message);
      })
      .finally(() => {
        this.search();
      });
  }

  displayMessage(message, level = EnumErrorLevel.ERROR) {
    toast.dismiss();

    switch (level) {
      case EnumErrorLevel.WARN:
        toast.warn(
          <ToastTemplate iconClass='fa-warning' text={message} />
        );
        break;
      case EnumErrorLevel.INFO:
        toast.info(
          <ToastTemplate iconClass='fa-info-circle' text={message} />
        );
        break;
      default:
        toast.error(
          <ToastTemplate iconClass='fa-exclamation-circle' text={message} />
        );
        break;
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
                      stopExecution={this.stopExecution}
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
  fetchProcessExecutions,
  resetFilters,
  setFilter,
  setPager,
  start,
  stop,
}, dispatch);

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps)(ProcessExplorer);
