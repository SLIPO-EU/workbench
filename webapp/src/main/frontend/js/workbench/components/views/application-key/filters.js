import * as React from 'react';

import {
  Button,
  Col,
  Row,
} from 'reactstrap';

import {
  SelectField,
  TextField,
} from '../../helpers/forms/fields/';

import {
  message,
} from '../../../service';

const statusValues = [
  { value: 'ALL', label: 'Select ...' },
  { value: 'ACTIVE', label: 'Active' },
  { value: 'REVOKED', label: 'Revoked' },
];

const resolveRevokedValue = (value) => {
  if (value === null) {
    return 'ALL';
  }
  return value ? 'REVOKED' : 'ACTIVE';
};

export default class Filters extends React.Component {

  constructor(props) {
    super(props);

    this.clear = this.clear.bind(this);
    this.search = this.search.bind(this);
  }

  clear() {
    this.props.resetFilters();
    this.props.query({ query: {} });
  }

  search(e) {
    const props = this.props;

    e.preventDefault();

    props.query({
      query: { ...this.props.filters },
    });
  }

  create() {
    message.error('error.NOT_IMPLEMENTED', 'fa-warning');
    // TODO : Implement ...
  }

  render() {
    const props = this.props;

    return (
      <form onSubmit={this.search}
      >
        <Row>
          <Col xs="12" md="2">
            <TextField
              id="applicationName"
              label="Application Name"
              value={props.filters.applicationName || ''}
              onChange={(val) => props.setFilter('applicationName', val)}
            />
          </Col>
          <Col xs="12" md="2">
            <TextField
              id="userName"
              label="Mapped Account"
              value={props.filters.userName || ''}
              onChange={(val) => props.setFilter('userName', val)}
            />
          </Col>
          <Col xs="12" md="2">
            <SelectField
              id="status"
              label="Status"
              value={resolveRevokedValue(props.filters.revoked)}
              onChange={(val) => props.setFilter('revoked', val)}
              options={statusValues}
            />
          </Col>
          <Col xs="12" md="6">
            <Button color="primary" onClick={this.create} style={{ marginTop: 30, float: 'right' }}>Create</Button>
            <Button color="warning" onClick={this.clear} style={{ marginTop: 30, float: 'right', marginRight: 10 }}>Clear</Button>
            <Button type="submit" style={{ marginTop: 30, float: 'right', marginRight: 10 }}>Search</Button>
          </Col>
        </Row>
      </form>
    );
  }
}
