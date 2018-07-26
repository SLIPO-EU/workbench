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
  TextArea,
} from '../helpers/forms/fields/textarea';

import {
  fetchEvents,
  resetFilters,
  setFilter,
  setPager,
  setSelected,
} from '../../ducks/ui/views/event';

import {
  Filters,
  Events,
} from "./event";


/**
 * Browse application events
 *
 * @class EventViewer
 * @extends {React.Component}
 */
class EventViewer extends React.Component {

  constructor(props) {
    super(props);

    this.refreshIntervalId = null;
  }

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
    this.props.fetchEvents({
      query: { ...this.props.filters },
    });
  }

  render() {
    const { selected } = this.props;
    const selectedItem = selected ? this.props.items.find((e) => e.id === selected.id) : null;
    const exception = selectedItem ? selectedItem.exception : null;

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
                      fetchEvents={this.props.fetchEvents}
                    />
                  </Col>
                </Row>
              </CardBody>
            </Card>
            <Card>
              <CardBody className="card-body">
                <Row className="mb-2">
                  <Col>
                    <Events
                      fetchEvents={this.props.fetchEvents}
                      filters={this.props.filters}
                      items={this.props.items}
                      pager={this.props.pager}
                      selected={this.props.selected}
                      setPager={this.props.setPager}
                      setSelected={this.props.setSelected}
                    />
                  </Col>
                </Row>
              </CardBody>
            </Card>
          </Col>
        </Row >
        {exception &&
          <Row>
            <Col className="col-12">
              <Card>
                <CardBody className="card-body">
                  <TextArea
                    rows={10}
                    id="exception"
                    readOnly={true}
                    value={exception}
                  />
                </CardBody>
              </Card>
            </Col>
          </Row>
        }
      </div>
    );
  }

}

const mapStateToProps = (state) => ({
  filters: state.ui.views.event.filters,
  items: state.ui.views.event.items,
  lastUpdate: state.ui.views.event.lastUpdate,
  loading: state.ui.views.event.loading,
  pager: state.ui.views.event.pager,
  selected: state.ui.views.event.selected,
});

const mapDispatchToProps = (dispatch) => bindActionCreators({
  fetchEvents,
  resetFilters,
  setFilter,
  setPager,
  setSelected,
}, dispatch);

export default ReactRedux.connect(mapStateToProps, mapDispatchToProps)(EventViewer);
