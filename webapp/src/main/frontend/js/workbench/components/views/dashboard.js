import * as React from 'react';
import * as ReactRedux from 'react-redux';
import { bindActionCreators } from 'redux';
import { Link } from 'react-router-dom';
import { FormattedTime } from 'react-intl';
import {
  Card as ReactCard, CardBlock, CardTitle, Row, Col,
  ButtonToolbar, Button, ButtonGroup, Label, Input
} from 'reactstrap';

import moment from 'moment';

import * as CardConfig from '../helpers/card-config';
import * as TableConfig from '../helpers/table-config';
import * as ChartConfig from '../helpers/chart-config';

import Card from '../helpers/card';
import Table from '../helpers/table';
import BarChart from '../helpers/chart';

import { fetchDashboardData } from '../../ducks/ui/views/dashboard';

class Dashboard extends React.Component {
  componentWillMount(){
    console.log("emphkamen stp mount:", this.props);
    this.props.fetchDashboardData(); 
  }
  
  render() {
    return (
      <div className="animated fadeIn">
        <div className="row">
          <div className="col-sm-12 col-md-6 col-lg-3">
            <Card { ...CardConfig.ResourceCardConfig(this.props.stats.resources)} />
          </div>
          <div className="col-sm-12 col-md-6 col-lg-3">
            <Card { ...CardConfig.JobCardConfig} />
          </div>
          <div className="col-sm-12 col-md-6 col-lg-3">
            <Card { ...CardConfig.QuotaCardConfig} />
          </div>
          <div className="col-sm-12 col-md-6 col-lg-3">
            <Card { ...CardConfig.EventCardConfig(this.props.stats.events)} />
          </div>
        </div>
        <Row>
          <Col sm="12" md="12" lg="6">
            <ReactCard>
              <CardBlock className="card-body">
                <Row>
                  <Col sm="5">
                    <CardTitle className="mb-0">Executed Processes</CardTitle>
                    <div className="small text-muted">Since <FormattedTime value={moment().add(-7, 'days').toDate()} day='numeric' month='numeric' year='numeric' /></div>
                  </Col>
                </Row>
                <div>
                  <BarChart { ...ChartConfig.JobSeries} />
                </div>
              </CardBlock>
            </ReactCard>
          </Col>
          <Col sm="12" md="12" lg="6">
            <ReactCard>
              <CardBlock className="card-body">
                <Row>
                  <Col sm="5">
                    <CardTitle className="mb-0">Quota Usage</CardTitle>
                    <div className="small text-muted">Since <FormattedTime value={moment().add(-7, 'days').toDate()} day='numeric' month='numeric' year='numeric' /></div>
                  </Col>
                </Row>
                <div>
                  <BarChart { ...ChartConfig.QuotaSeries} />
                </div>
              </CardBlock>
            </ReactCard>
          </Col>
        </Row>
        <Row>
          <Col className="col-sm-12 col-md-12 col-lg-6">
            <ReactCard>
              <CardBlock className="card-body">
                <Row>
                  <Col sm="5">
                    <CardTitle className="mb-0">Process Explorer</CardTitle>
                    <div className="small text-muted">Last Update: <FormattedTime value={moment().toDate()} day='numeric' month='numeric' year='numeric' /></div>
                  </Col>
                  <Col sm="7" className="d-none d-sm-inline-block">
                    <ButtonToolbar className="float-right" aria-label="Toolbar with button groups">
                      <ButtonGroup data-toggle="buttons" aria-label="First group">
                        <Label htmlFor="option1" className="btn btn-outline-secondary active" check>
                          <Input type="radio" name="jobStatusFilter" id="option1" /> All
                        </Label>
                        <Label htmlFor="option2" className="btn btn-outline-secondary">
                          <Input type="radio" name="jobStatusFilter" id="option2" /> Completed
                        </Label>
                        <Label htmlFor="option3" className="btn btn-outline-secondary">
                          <Input type="radio" name="jobStatusFilter" id="option3" /> Running
                        </Label>
                        <Label htmlFor="option4" className="btn btn-outline-secondary">
                          <Input type="radio" name="jobStatusFilter" id="option4" /> Failed
                        </Label>
                      </ButtonGroup>
                    </ButtonToolbar>
                  </Col>
                </Row>
                <div>
                  <Table
                    data={TableConfig.JobGridData}
                    columns={TableConfig.JobGridColumns}
                  />
                </div>
              </CardBlock>
            </ReactCard>
          </Col>
          <Col className="col-sm-12 col-md-12 col-lg-6">
            <ReactCard>
              <CardBlock className="card-body">
                <Row>
                  <Col sm="5">
                    <CardTitle className="mb-0">Resources</CardTitle>
                    <div className="small text-muted">Last Update: <FormattedTime value={moment().toDate()} day='numeric' month='numeric' year='numeric' /></div>
                  </Col>
                  <Col sm="7" className="d-none d-sm-inline-block">
                    <ButtonToolbar className="float-right" aria-label="Toolbar with button groups">
                      <ButtonGroup data-toggle="buttons" aria-label="First group">
                        <Label htmlFor="option1" className="btn btn-outline-secondary active" check>
                          <Input type="radio" name="resourceFilter" id="option1" /> All
                        </Label>
                        <Label htmlFor="option2" className="btn btn-outline-secondary">
                          <Input type="radio" name="resourceFilter" id="option2" /> New
                        </Label>
                        <Label htmlFor="option3" className="btn btn-outline-secondary">
                          <Input type="radio" name="resourceFilter" id="option3" /> Updated
                        </Label>
                      </ButtonGroup>
                    </ButtonToolbar>
                  </Col>
                </Row>
                <div>
                  <Table
                    data={TableConfig.ResourceGridData(this.props.resources)}
                    columns={TableConfig.ResourceGridColumns}
                  />
                </div>
              </CardBlock>
            </ReactCard>
          </Col>
        </Row >
        <Row>
          <Col className="col-12">
            <ReactCard>
              <CardBlock className="card-body">
                <Row>
                  <Col sm="5">
                    <CardTitle className="mb-0">Events</CardTitle>
                    <div className="small text-muted">Last Update: <FormattedTime value={moment().toDate()} day='numeric' month='numeric' year='numeric' /></div>
                  </Col>
                  <Col sm="7" className="d-none d-sm-inline-block">
                    <ButtonToolbar className="float-right" aria-label="Toolbar with button groups">
                      <ButtonGroup data-toggle="buttons" aria-label="First group">
                        <Label htmlFor="option1" className="btn btn-outline-secondary active" check>
                          <Input type="radio" name="resourceFilter" id="option1" /> All
                        </Label>
                        <Label htmlFor="option2" className="btn btn-outline-secondary">
                          <Input type="radio" name="resourceFilter" id="option2" /> Error
                        </Label>
                        <Label htmlFor="option3" className="btn btn-outline-secondary">
                          <Input type="radio" name="resourceFilter" id="option3" /> Warning
                        </Label>
                      </ButtonGroup>
                    </ButtonToolbar>
                  </Col>
                </Row>
                <div>
                  <Table
                    data={TableConfig.EventGridData(this.props.events)}
                    columns={TableConfig.EventGridColumns}
                  />
                </div>
              </CardBlock>
            </ReactCard>
          </Col>
        </Row >
      </div >
    );
  }
}

//export default Dashboard;



const mapStateToProps = (state) => ({
  stats: state.ui.views.dashboard.statistics,
  resources: state.ui.views.dashboard.resources,
  events: state.ui.views.dashboard.events,
  //pager: state.ui.views.resources.explorer.pager,
  //filters: state.ui.views.resources.explorer.filters,
  //givenName: state.user.profile.givenName,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({ fetchDashboardData }, dispatch);

/*const mergeProps = (stateProps, dispatchProps, ownProps) => {
  return {
    ...stateProps,
    ...dispatchProps,
    ...ownProps,
  };
};*/

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps)(Dashboard);
