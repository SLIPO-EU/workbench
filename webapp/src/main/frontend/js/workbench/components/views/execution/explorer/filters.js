import * as React from 'react';

import {
  Button,
  Col,
  Row,
} from 'reactstrap';

import {
  SelectField,
  TextField,
} from '../../../helpers/forms/fields/';

// TODO : Load during configuration
const supportedTask = [
  { value: 'REGISTRATION', label: 'Registration' },
  { value: 'DATA_INTEGRATION', label: 'Data Integration' },
];

const supportedStatus = [
  { value: 'ABANDONED', label: 'ABANDONED' },
  { value: 'COMPLETED', label: 'COMPLETED' },
  { value: 'FAILED', label: 'FAILED' },
  { value: 'STARTED', label: 'STARTED' },
  { value: 'STARTING', label: 'STARTING' },
  { value: 'STOPPED', label: 'STOPPED' },
  { value: 'STOPPING', label: 'STOPPING' },
  { value: 'UNKNOWN', label: 'UNKNOWN' },
];

export default class Filters extends React.Component {

  constructor(props) {
    super(props);

    this.clear = this.clear.bind(this);
    this.search = this.search.bind(this);
  }

  clear() {
    this.props.resetFilters();
    this.props.fetchExecutions({query: {}});
  }

  search(e) {
    const props = this.props;

    e.preventDefault();

    this.props.fetchExecutions({
      query: {...this.props.filters},
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
              label="Name"
              value={props.filters.name || ''}
              onChange={(val) => props.setFilter('name', val)}
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
            <SelectField
              id="taskType"
              label="Task"
              value={props.filters.taskType || ''}
              onChange={(val) => props.setFilter('taskType', val)}
              options={supportedTask}
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
