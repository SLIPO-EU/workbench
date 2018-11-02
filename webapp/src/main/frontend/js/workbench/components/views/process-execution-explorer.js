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
  buildPath
} from '../../model/routes';

import {
  EnumErrorLevel,
  UPDATE_INTERVAL_SECONDS,
} from '../../model';

import {
  Filters,
  ProcessExecutions,
} from "./execution/explorer";

import {
  exportMap,
  stop,
} from '../../ducks/ui/views/process-explorer';

import {
  fetchExecutions,
  resetFilters,
  setExpanded,
  setFilter,
  setPager,
  setSelected,
} from '../../ducks/ui/views/process-execution-explorer';

import {
  message,
} from '../../service';

/**
 * Browse and manage process executions
 *
 * @class ProcessExecutionExplorer
 * @extends {React.Component}
 */
class ProcessExecutionExplorer extends React.Component {

  constructor(props) {
    super(props);

    this.editProcess = this.editProcess.bind(this);
    this.exportMap = this.exportMap.bind(this);
    this.stopExecution = this.stopExecution.bind(this);
    this.viewExecution = this.viewExecution.bind(this);
    this.viewMap = this.viewMap.bind(this);

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
    this.props.fetchExecutions({
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
   * Display details about the selected execution instance
   *
   * @param {any} id
   * @param {any} version
   * @param {any} execution
   * @memberof ProcessExecutionExplorer
   */
  viewExecution(id, version, execution) {
    const path = buildPath(DynamicRoutes.ProcessExecutionViewer, [id, version, execution]);

    this.props.history.push(path);
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
        message.error(err.message);
      })
      .finally(() => {
        this.search();
      });
  }

  exportMap(id, version, executionId) {
    this.props.exportMap(id, version, executionId)
      .then(() => {
        message.info('Process execution export has started successfully');
      }).catch((err) => {
        message.error(err.message);
      });
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
                <Row style={{ height: 100 }} className="mb-2">
                  <Col>
                    <Filters
                      filters={this.props.filters}
                      setFilter={this.props.setFilter}
                      resetFilters={this.props.resetFilters}
                      fetchExecutions={this.props.fetchExecutions}
                    />
                  </Col>
                </Row>
                <Row className="mb-2">
                  <Col>
                    <ProcessExecutions
                      expanded={this.props.expanded}
                      editProcess={this.editProcess}
                      exportMap={this.exportMap}
                      fetchExecutions={this.props.fetchExecutions}
                      filters={this.props.filters}
                      items={this.props.items}
                      pager={this.props.pager}
                      selected={this.props.selected}
                      setExpanded={this.props.setExpanded}
                      setPager={this.props.setPager}
                      setSelected={this.props.setSelected}
                      stopExecution={this.stopExecution}
                      user={this.props.user}
                      viewExecution={this.viewExecution}
                      viewMap={this.viewMap}
                    />
                  </Col>
                </Row>
              </CardBody>
            </Card>
          </Col>
        </Row >
      </div>
    );
  }

}

const mapStateToProps = (state) => ({
  expanded: state.ui.views.execution.explorer.expanded,
  filters: state.ui.views.execution.explorer.filters,
  items: state.ui.views.execution.explorer.items,
  lastUpdate: state.ui.views.execution.explorer.lastUpdate,
  pager: state.ui.views.execution.explorer.pager,
  selected: state.ui.views.execution.explorer.selected,
  user: state.user.profile,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  fetchExecutions,
  exportMap,
  resetFilters,
  setExpanded,
  setFilter,
  setPager,
  setSelected,
  stop,
}, dispatch);

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps)(ProcessExecutionExplorer);
