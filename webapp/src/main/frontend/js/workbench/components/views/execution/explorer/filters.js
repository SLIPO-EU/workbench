import * as React from 'react';
import * as PropTypes from 'prop-types';

import {
  Button,
  Col,
  Row,
} from 'reactstrap';

import {
  EnumTaskType,
} from '../../../../model/process-designer/enum';

import {
  SelectField,
  TextField,
} from '../../../helpers/forms/fields/';

// TODO : Load during configuration
const supportedStatus = [
  { value: null, label: 'Select...' },
  { value: 'COMPLETED', label: 'COMPLETED' },
  { value: 'FAILED', label: 'FAILED' },
  { value: 'RUNNING', label: 'RUNNING' },
  { value: 'STOPPED', label: 'STOPPED' },
  { value: 'UNKNOWN', label: 'UNKNOWN' },
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
    const _t = this.context.intl.formatMessage;
    const supportedTasks = [
      { value: null, label: 'Select...' },
      ...Object.keys(EnumTaskType)
        .filter(key => key !== EnumTaskType.API)
        .map(key => ({ value: key, label: _t({ id: `enum.taskType.${key}` }) }))
    ];

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
              options={supportedTasks}
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
