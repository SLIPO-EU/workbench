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
  UPDATE_INTERVAL_SECONDS,
} from '../../model';

import {
  query,
  resetFilters,
  revoke,
  setExpanded,
  setFilter,
  setPager,
  setSelected,
} from '../../ducks/ui/views/application-key';

import {
  Filters,
  ApplicationKeys,
} from "./application-key";

import {
  message,
} from '../../service';

/**
 * Component for managing application keys
 *
 * @class ApplicationKeyViewer
 * @extends {React.Component}
 */
class ApplicationKeyViewer extends React.Component {

  constructor(props) {
    super(props);
  }

  componentWillMount() {
    this.search();
  }

  search() {
    this.props.query({
      query: { ...this.props.filters },
    });
  }

  revoke(id) {
    message.error('error.NOT_IMPLEMENTED', 'fa-warning');
    // TODO : Implement ...
    //this.props.revoke(id);
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
                      query={this.props.query}
                    />
                  </Col>
                </Row>
              </CardBody>
            </Card>
            <Card>
              <CardBody className="card-body">
                <Row className="mb-2">
                  <Col>
                    <ApplicationKeys
                      expanded={this.props.expanded}
                      filters={this.props.filters}
                      items={this.props.items}
                      pager={this.props.pager}
                      query={this.props.query}
                      revoke={(id) => this.revoke(id)}
                      selected={this.props.selected}
                      setExpanded={this.props.setExpanded}
                      setPager={this.props.setPager}
                      setSelected={this.props.setSelected}
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
  expanded: state.ui.views.applicationKey.expanded,
  filters: state.ui.views.applicationKey.filters,
  items: state.ui.views.applicationKey.items,
  lastUpdate: state.ui.views.applicationKey.lastUpdate,
  loading: state.ui.views.applicationKey.loading,
  pager: state.ui.views.applicationKey.pager,
  selected: state.ui.views.applicationKey.selected,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  query,
  resetFilters,
  revoke,
  setExpanded,
  setFilter,
  setPager,
  setSelected,
}, dispatch);

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps)(ApplicationKeyViewer);
