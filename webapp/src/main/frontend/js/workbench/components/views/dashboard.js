import * as React from 'react';
import * as ReactRedux from 'react-redux';

import {
  bindActionCreators,
} from 'redux';

import {
  Link,
} from 'react-router-dom';

import {
  FormattedTime,
  injectIntl,
} from 'react-intl';

import {
  Col,
  CardBody,
  Row,
} from 'reactstrap';

import moment from 'moment';

import {
  Roles,
} from '../../model';

import {
  BarChart,
  Card,
  DashboardCard,
  SecureContent,
  Table,
} from '../helpers';

import {
  changeDashboardFilter,
  fetchDashboardData,
  resetSelectedEvent,
  selectEvent,
} from '../../ducks/ui/views/dashboard';

import * as CardConfig from '../helpers/card-config';
import * as TableConfig from '../helpers/table-config';
import * as ChartConfig from '../helpers/chart-config';
import * as DashboardCardConfig from '../helpers/dashboardCard-config';

const UPDATE_INTERVAL_SECONDS = 300;

class Dashboard extends React.Component {

  constructor(props) {
    super(props);

    this.refreshIntervalId = null;
  }

  componentWillMount() {
    this.refreshIntervalId = setInterval(() => {
      this.props.fetchDashboardData();
    }, UPDATE_INTERVAL_SECONDS * 1000);

    this.props.fetchDashboardData();
  }

  componentWillUnmount() {
    if (this.refreshIntervalId) {
      clearInterval(this.refreshIntervalId);
      this.refreshIntervalId = null;
    }
  }

  render() {
    return (
      <div className="animated fadeIn">
        <Row>
          <Col className="col-sm-12 col-md-6 col-lg-3">
            <Card {...CardConfig.ResourceCardConfig(this.props.stats.resources, this.props.intl)} />
          </Col>
          <Col className="col-sm-12 col-md-6 col-lg-3">
            <Card {...CardConfig.JobCardConfig(this.props.stats.processes, this.props.intl)} />
          </Col>
          <Col className="col-sm-12 col-md-6 col-lg-3">
            <Card {...CardConfig.SystemCardConfig(this.props.stats.system)} />
          </Col>
          <Col className="col-sm-12 col-md-6 col-lg-3">
            <Card {...CardConfig.EventCardConfig(this.props.stats.events, this.props.intl)} />
          </Col>
        </Row>
        <Row>
          <Col className="col-sm-12 col-md-12 col-lg-6">
            <DashboardCard
              {...DashboardCardConfig.DashboardProcessExplorerConfig}
              updatedOn={new Date()}
              filterChange={this.props.changeDashboardFilter}
              filterValue={this.props.filters.processExplorer}
            >
              <Table
                data={TableConfig.processDataMapper(this.props.processes)}
                columns={TableConfig.ProcessExecutionGridColumns}
                minRows={10}
                showPagination={true}
              />
            </DashboardCard>
          </Col>
          <Col className="col-sm-12 col-md-12 col-lg-6">
            <DashboardCard
              {...DashboardCardConfig.DashboardResourcesConfig}
              updatedOn={new Date()}
              filterChange={this.props.changeDashboardFilter}
              filterValue={this.props.filters.resources}
            >
              <Table
                data={TableConfig.resourceDataMapper(this.props.resources)}
                columns={TableConfig.ResourceGridColumns}
                minRows={10}
                showPagination={true}
              />
            </DashboardCard>
          </Col>
        </Row >
        <SecureContent role={Roles.ADMIN}>
          <Row>
            <Col className="col-12">
              <DashboardCard
                {...DashboardCardConfig.DashboardEventsConfig}
                updatedOn={new Date()}
                filterChange={this.props.changeDashboardFilter}
                filterValue={this.props.filters.events}
              >
                <Table
                  data={TableConfig.eventDataMapper(this.props.events)}
                  columns={TableConfig.EventGridColumns}
                  minRows={10}
                  showPagination={true}
                  getTrProps={(state, rowInfo) => ({
                    onClick: (e) => {
                      this.props.selectEvent(rowInfo.viewIndex, rowInfo.original);
                    }
                  })}
                  onPageChange={
                    (pageIndex) => {
                      this.props.resetSelectedEvent();
                    }
                  }
                  expanded={
                    (this.props.selectedEvent) ?
                      {
                        [this.props.selectedEvent.index]: true
                      } :
                      {}
                  }
                  SubComponent={
                    row => {
                      if (this.props.selectedEvent &&
                          this.props.selectedEvent.index === row.viewIndex &&
                          this.props.selectedEvent.event.message) {
                        return (
                          <CardBody>
                            <div className="font-weight-bold mb-2">Message:</div>
                            <div className="font-weight-italic" style={{ whiteSpace: 'pre-wrap' }}>{this.props.selectedEvent.event.message}</div>
                          </CardBody>
                        );
                      }
                      return null;
                    }
                  }
                />
              </DashboardCard>
            </Col>
          </Row >
        </SecureContent>
      </div >
    );
  }
}

const mapStateToProps = (state) => ({
  events: state.ui.views.dashboard.events,
  filters: state.ui.views.dashboard.filters,
  processes: state.ui.views.dashboard.processes,
  resources: state.ui.views.dashboard.resources,
  selectedEvent: state.ui.views.dashboard.selectedEvent,
  stats: state.ui.views.dashboard.statistics,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  changeDashboardFilter,
  fetchDashboardData,
  resetSelectedEvent,
  selectEvent,
}, dispatch);

const mergeProps = (stateProps, dispatchProps, ownProps) => {
  let resVisible, procVisible;
  switch (stateProps.filters.resources) {
    case 'all':
      resVisible = stateProps.resources;
      break;
    case 'new':
      resVisible = stateProps.resources.filter(resource => resource.createdOn === resource.updatedOn);
      break;
    case 'updated':
      resVisible = stateProps.resources.filter(resource => resource.createdOn !== resource.updatedOn);
  }
  switch (stateProps.filters.processExplorer) {
    case 'allProcess':
      procVisible = stateProps.processes;
      break;
    case 'completed':
      procVisible = stateProps.processes.filter(process => process.status == 'COMPLETED');
      break;
    case 'running':
      procVisible = stateProps.processes.filter(process => process.status == 'RUNNING');
      break;
    case 'failed':
      procVisible = stateProps.processes.filter(process => process.status == 'FAILED');
  }

  return {
    ...stateProps,
    ...dispatchProps,
    ...ownProps,
    events: stateProps.filters.events === "ALL" ? stateProps.events : stateProps.events.filter(event => event.level === stateProps.filters.events),
    resources: resVisible,
    processes: procVisible
  };
};

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps, mergeProps)(injectIntl(Dashboard));
