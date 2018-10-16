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

import * as Roles from '../../../../model/role';

import {
  SecureContent,
} from '../../../helpers';

import {
  SelectField,
  TextField,
} from '../../../helpers/forms/fields/';

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
    this.props.fetchProcesses({ query: {} });
  }

  search(e) {
    const props = this.props;

    e.preventDefault();

    props.fetchProcesses({
      query: { ...this.props.filters },
    });
  }

  render() {
    const props = this.props;
    const _t = this.context.intl.formatMessage;
    const supportedTasks = [
      { value: null, label: 'Select...' },
      ...Object.keys(EnumTaskType).map(key => ({ value: key, label: _t({ id: `enum.taskType.${key}` }) }))
    ];

    return (
      <form onSubmit={this.search}
      >
        <Row>
          <Col xs="12" md="3">
            <TextField
              id="name"
              label="Name"
              value={props.filters.name || ''}
              onChange={(val) => props.setFilter('name', val)}
            />
          </Col>
          <Col xs="12" md="3">
            <SecureContent roles={[Roles.ADMIN]}>
              <SelectField
                id="taskType"
                label="Task"
                value={props.filters.taskType || ''}
                onChange={(val) => props.setFilter('taskType', val)}
                options={supportedTasks}
              />
            </SecureContent>
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
