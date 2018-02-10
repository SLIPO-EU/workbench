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
  Filters,
  ProcessExecutions,
} from "./execution/explorer";


import {
  fetchExecutions,
  resetFilters,
  setExpanded,
  setFilter,
  setPager,
  setSelected,
} from '../../ducks/ui/views/process-execution-explorer';

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
    this.viewExecution = this.viewExecution.bind(this);
  }

  /**
   * Initializes a request for fetching process data, optionally using any
   * existing search criteria
   *
   * @memberof ProcessExplorer
   */
  componentWillMount() {
    this.props.fetchExecutions({
      query: {...this.props.filters},
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
                      fetchExecutions={this.props.fetchExecutions}
                      filters={this.props.filters}
                      items={this.props.items}
                      pager={this.props.pager}
                      selected={this.props.selected}
                      setExpanded={this.props.setExpanded}
                      setPager={this.props.setPager}
                      setSelected={this.props.setSelected}
                      viewExecution={this.viewExecution}
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
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  fetchExecutions,
  resetFilters,
  setExpanded,
  setFilter,
  setPager,
  setSelected,
}, dispatch);

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps)(ProcessExecutionExplorer);
