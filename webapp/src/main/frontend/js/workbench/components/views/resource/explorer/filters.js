import * as React from 'react';
import { Row, Col, Button } from 'reactstrap';

import { TextField, SelectField } from '../../../helpers/forms/fields/'; 

const supportedFiles = [
  { value: 'SHAPEFILE' },
  { value: 'CSV' },
  { value: 'GPX' },
  { value: 'GEOJSON' },
  { value: 'OSM' },
  { value: 'RDF' },
];


export default function Filters(props) {
  return (
    <form onSubmit={(e) => {
      e.preventDefault();
      props.fetchResources({ name: props.filters.name });
    }}
    >
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
          <TextField
            id="description"
            label="Description"
            value={props.filters.description || ''}
            onChange={(val) => props.setFilter('description', val)}
          />
        </Col>
        <Col>
          <SelectField
            id="format"
            label="Format"
            value={props.filters.format || ''}
            onChange={(val) => props.setFilter('format', val)}
            options={supportedFiles} 
          />
        </Col>
        <Col>
          <Button color="warning" onClick={props.resetFilters} style={{ marginTop: 30, float: 'right'}}>Clear</Button>
          <Button type="submit" style={{ marginTop: 30, float: 'right', marginRight: 10 }}>Search</Button>
        </Col>
      </Row>
    </form>
  );
}
