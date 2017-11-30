import * as React from 'react';
import * as ReactRedux from 'react-redux';
import {
  Card as ReactCard, CardBody, CardTitle, Row, Col,
  ButtonToolbar, Button, ButtonGroup, Label, Input
} from 'reactstrap';
import { FormattedTime } from 'react-intl';

import moment from 'moment';

import PropTypes from 'prop-types';


const DashboardCard = (props) => (
  <ReactCard>
    <CardBody className="card-body" >
      <Row>
        <Col sm="5">
          <CardTitle className="mb-0">{props.name}</CardTitle>
          <div className="small text-muted">Last Update: <FormattedTime value={moment(props.changedOn).toDate()} day='numeric' month='numeric' year='numeric' /></div>
        </Col>
        <Col sm="7" className="d-none d-sm-inline-block">
          <ButtonToolbar className="float-right" aria-label="Toolbar with button groups">
            <ButtonGroup data-toggle="buttons" aria-label="First group">
              { props.cardFilters.map(cardFilter => (
                <Label key={cardFilter.id} htmlFor={cardFilter.id} className={ cardFilter.id === props.filterValue ? "btn btn-outline-secondary active" :"btn btn-outline-secondary "} check={cardFilter.id === props.filterValue}>
                  <Input type="radio" name="resourceFilter" id={cardFilter.id} onChange={() => props.filterChange(props.cardname, cardFilter.id) }/> {cardFilter.name}
                </Label>))
              }
            </ButtonGroup>
          </ButtonToolbar>
        </Col>
      </Row>
      <div>
        {props.children}
      </div>
    </CardBody>
  </ReactCard>
);

DashboardCard.propTypes = {
  filterChange: PropTypes.func,
  cardFilters: PropTypes.array.isRequired,
  filterValue: PropTypes.string.isRequired,
  id: PropTypes.string,
};
DashboardCard.defaultProps = {
  cardFilters: [],
  filterValue:'all',
};

export default DashboardCard;
