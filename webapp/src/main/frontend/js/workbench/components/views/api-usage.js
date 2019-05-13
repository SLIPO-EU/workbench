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
  buildPath,
  DynamicRoutes,
  UPDATE_INTERVAL_SECONDS,
} from '../../model';

import {
  Filters,
  ProcessExecutions,
} from "./process/api";

import {
  stop,
} from '../../ducks/ui/views/process-explorer';

import {
  fetchExecutions,
  resetFilters,
  setExpanded,
  setFilter,
  setPager,
  setSelected,
} from '../../ducks/ui/views/api-usage';

import {
  message,
} from '../../service';

/**
 * Browse API usage
 *
 * @class ApiUsage
 * @extends {React.Component}
 */
class ApiUsage extends React.Component {

  constructor(props) {
    super(props);

    this.stopExecution = this.stopExecution.bind(this);
    this.viewExecution = this.viewExecution.bind(this);

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

  stopExecution(id, version) {
    this.props.stop(id, version)
      .catch((err) => {
        message.error(err.message);
      })
      .finally(() => {
        this.search();
      });
  }

  viewExecution(processId) {
    const path = buildPath(DynamicRoutes.ApiExecutionViewer, [processId]);

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
                      fetchExecutions={this.props.fetchExecutions}
                      filters={this.props.filters}
                      items={this.props.items}
                      pager={this.props.pager}
                      selected={this.props.selected}
                      setExpanded={this.props.setExpanded}
                      setPager={this.props.setPager}
                      setSelected={this.props.setSelected}
                      stopExecution={this.stopExecution}
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
  expanded: state.ui.views.admin.api.expanded,
  filters: state.ui.views.admin.api.filters,
  items: state.ui.views.admin.api.items,
  lastUpdate: state.ui.views.admin.api.lastUpdate,
  pager: state.ui.views.admin.api.pager,
  selected: state.ui.views.admin.api.selected,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  fetchExecutions,
  resetFilters,
  setExpanded,
  setFilter,
  setPager,
  setSelected,
  stop,
}, dispatch);

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps)(ApiUsage);
