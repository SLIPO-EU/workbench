import * as React from 'react';
import * as PropTypes from 'prop-types';

import {
  Button,
  Col,
  Row,
} from 'reactstrap';

import {
  SelectField,
  TextField,
} from '../../../helpers/forms/fields/';

const supportedStatus = [
  { value: null, label: 'Select...' },
  { value: 'COMPLETED', label: 'COMPLETED' },
  { value: 'FAILED', label: 'FAILED' },
  { value: 'RUNNING', label: 'RUNNING' },
  { value: 'STOPPED', label: 'STOPPED' },
  { value: 'UNKNOWN', label: 'UNKNOWN' },
];

const supportedOperations = [
  { value: null, label: 'Select...' },
  { value: 'TRANSFORM', label: 'Transform' },
  { value: 'INTERLINK', label: 'Interlink' },
  { value: 'FUSION', label: 'Fusion' },
  { value: 'ENRICHMENT', label: 'Enrichment' },
];

export default class Filters extends React.Component {

  constructor(props) {
    super(props);

    this.clear = this.clear.bind(this);
    this.search = this.search.bind(this);
  }

  static contextTypes = {
    intl: PropTypes.object,
  }

  clear() {
    this.props.resetFilters();
    this.props.fetchExecutions({ query: {} });
  }

  search(e) {
    e.preventDefault();

    this.props.fetchExecutions({
      query: { ...this.props.filters },
    });
  }

  render() {
    const props = this.props;

    return (
      <form onSubmit={this.search}>
        <Row className="mb-2">
          <Col>
            <TextField
              id="name"
              label="Application Name"
              value={props.filters.name || ''}
              onChange={(val) => props.setFilter('name', val)}
            />
          </Col>
          <Col>
            <SelectField
              id="operation"
              label="Operation"
              value={props.filters.operation || ''}
              onChange={(val) => props.setFilter('operation', val)}
              options={supportedOperations}
            />
          </Col>
          <Col>
            <SelectField
              id="status"
              label="Status"
              value={props.filters.status || ''}
              onChange={(val) => props.setFilter('status', val)}
              options={supportedStatus}
            />
          </Col>
          <Col>
            <Button color="warning" onClick={this.clear} style={{ marginTop: 30, float: 'right' }}>Clear</Button>
            <Button type="submit" style={{ marginTop: 30, float: 'right', marginRight: 10 }}>Search</Button>
          </Col>
        </Row>
      </form>
    );
  }
}
