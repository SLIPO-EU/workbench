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
const supportedFiles = [
  { value: 'SHAPEFILE' },
  { value: 'CSV' },
  { value: 'GPX' },
  { value: 'GEOJSON' },
  { value: 'OSM' },
  { value: 'RDF' },
];

const supportedTypes = [
  { value: 'POI_DATA', label: 'POI Data' },
  { value: 'POI_LINKED_DATA', label: 'Linked Data' },
];

export default class Filters extends React.Component {

  constructor(props) {
    super(props);

    this.clear = this.clear.bind(this);
    this.search = this.search.bind(this);
  }

  clear() {
    this.props.resetFilters();
    this.props.fetchResources({});
  }

  search(e) {
    const props = this.props;

    e.preventDefault();

    this.props.fetchResources({
      ...this.props.filters,
    });
  }

  render() {
    const props = this.props;

    return (
      <form onSubmit={this.search}>
        <Row>
          <Col xs={6} md={3}>
            <TextField
              id="name"
              label="Name"
              value={props.filters.name || ''}
              onChange={(val) => props.setFilter('name', val)}
            />
          </Col>
          <Col xs={6} md={3}>
            <TextField
              id="description"
              label="Description"
              value={props.filters.description || ''}
              onChange={(val) => props.setFilter('description', val)}
            />
          </Col>
          <Col xs={6} md={3}>
            <SelectField
              id="format"
              label="Initial Format"
              value={props.filters.format || ''}
              onChange={(val) => props.setFilter('format', val)}
              options={supportedFiles}
            />
          </Col>
          <Col xs={6} md={3}>
            <SelectField
              id="type"
              label="Resource Type"
              value={props.filters.type || ''}
              onChange={(val) => props.setFilter('type', val)}
              options={supportedTypes}
            />
          </Col>
        </Row>
        <Row className="mb-2 float-md-right">
          <Col>
            <Button type="submit" style={{ marginRight: 10 }}>Search</Button>
            <Button color="warning" onClick={this.clear}>Clear</Button>
          </Col>
        </Row>
      </form>
    );
  }
}
