import * as React from 'react';

import {
  Button,
  Col,
  Row,
} from 'reactstrap';

import {
  EnumErrorLevel,
} from '../../../model';

import {
  SelectField,
  TextField,
} from '../../helpers/forms/fields/';

const supportedLevels = [
  { value: null, label: 'Select...' },
  ...Object.keys(EnumErrorLevel).map(key => ({ value: key, label: EnumErrorLevel[key] }))
];

export default class Filters extends React.Component {

  constructor(props) {
    super(props);

    this.clear = this.clear.bind(this);
    this.search = this.search.bind(this);
  }

  clear() {
    this.props.resetFilters();
    this.props.fetchEvents({ query: {} });
  }

  search(e) {
    const props = this.props;

    e.preventDefault();

    props.fetchEvents({
      query: { ...this.props.filters },
    });
  }

  render() {
    const props = this.props;

    return (
      <form onSubmit={this.search}
      >
        <Row>
          <Col xs="12" md="3">
            <TextField
              id="userName"
              label="User Name"
              value={props.filters.userName || ''}
              onChange={(val) => props.setFilter('userName', val)}
            />
          </Col>
          <Col xs="12" md="3">
            <SelectField
              id="level"
              label="Level"
              value={props.filters.level || ''}
              onChange={(val) => props.setFilter('level', val)}
              options={supportedLevels}
            />
          </Col>
          <Col xs="12" md="6">
            <Button color="warning" onClick={this.clear} style={{ marginTop: 30, float: 'right' }}>Clear</Button>
            <Button type="submit" style={{ marginTop: 30, float: 'right', marginRight: 10 }}>Search</Button>
          </Col>
        </Row>
      </form>
    );
  }
}
