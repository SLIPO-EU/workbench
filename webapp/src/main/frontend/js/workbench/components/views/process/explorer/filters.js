import * as React from 'react';
import { Row, Col, Button } from 'reactstrap';

import { TextField, SelectField } from '../../../helpers/forms/fields/';

export default class Filters extends React.Component {

  constructor(props) {
    super(props);

    this.clear = this.clear.bind(this);
    this.search = this.search.bind(this);
  }

  clear() {
    this.props.resetFilters();
    this.props.fetchProcesses({});
  }

  search(e) {
    const props = this.props;

    e.preventDefault();

    props.fetchProcesses({
      ...this.props.filters,
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
              id="name"
              label="Name"
              value={props.filters.name || ''}
              onChange={(val) => props.setFilter('name', val)}
            />
          </Col>
          <Col xs="12" md="9">
            <Button color="warning" onClick={this.clear} style={{ marginTop: 30, float: 'right' }}>Clear</Button>
            <Button type="submit" style={{ marginTop: 30, float: 'right', marginRight: 10 }}>Search</Button>
          </Col>
        </Row>
      </form>
    );
  }
}
