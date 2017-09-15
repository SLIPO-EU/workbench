import React from 'react';
import PropTypes from 'prop-types';
import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { Button } from 'reactstrap';

import { createForm, SelectField, TextField, PasswordField } from '../forms';

import { setForm, resetForm } from '../../ducks/forms';

const model = {
  'serialization': 'N-TRIPLES',
  'targetOntology': 'Virtuoso',
  'sourceCRS': 'EPSG:2100',
  'targetCRS': 'EPSG:4326',
};

const validator = function(values, getState) {
  const errors = {};
  if (!values['featureName']) {
    errors['featureName'] = 'Required for constructing the resource URI';
  }
  if (!values['targetOntology']) {
    errors['targetOntology'] = 'Required';
  }

  if (Object.keys(errors).length) {
    throw errors;
  }
};

const TripleGeoForm = createForm('triplegeo', model, validator);

function FormContainer(props) {
  return (
    <TripleGeoForm
      onSuccess={(values) => {
        console.log('submitted ', values, ' successfully!');
      }}
      onFailure={(err) => {
        console.warn('failed with errors ', err);
      }}
    >
      <SelectField
        id="serialization"
        label="Serialization format"
        help="Specify export serialization for the output file"
        options={[
          { value: 'RDF/XML' },
          { value: 'RDF/XML-ABBREV' },
          { value: 'N-TRIPLES' },
          { value: 'TURTLE' },
          { value: 'N3' },
        ]}
      />

      <SelectField
        id="targetOntology"
        label="Ontology Type"
        help="Specify the type of the spatial ontology where the exported data will refer to"
        options={[
          { value: 'GeoSPARQL' },
          { value: 'Virtuoso' },
          { value: 'WGS84' },
        ]}
      />

      <div>
        <h4>Namespace parameters</h4>
        <hr />
      </div>

      <TextField
        id="featureName"
        label="Feature name"
        help="Parameter that specifies a user-defined name for the resources that will be created"
      />

      <TextField
        id="nsFeatureURI"
        label="Feature URI"
        help="Specify the common URI namespace for all generated resources"
      />

      <TextField
        id="prefixFeatureNS"
        label="Feature Prefix"
        help="Define a prefix name for the utilized URI namespace (i.e., the previously declared with nsFeatureURI)"
      />

      <TextField
        id="nsGeometryURI"
        label="Geometry URI"
        help="Specify the namespace for the underlying geospatial ontology"
      />

      <TextField
        id="prefixGeometryNS"
        label="Geometry Prefix"
        help="Define a prefix name for the geospatial ontology (i.e., the previously declared with nsGeometryURI)"
      />


      <div>
        <h4>Spatial Reference parameters</h4>
        <hr />
      </div>

      <TextField
        id="sourceCRS"
        label="Source CRS"
        help=""
      />

      <TextField
        id="targetCRS"
        label="Target CRS"
        help=""
      />

      <Button type="submit">Submit</Button>
    </TripleGeoForm>
  );
}

/**
 * Designer for creating and updating POI data integration processes
 * 
 * @class ProcessDesigner
 * @extends {React.Component}
 */
export default class TripleGEO extends React.Component {
  render() {
    return (
      <div className="animated fadeIn">
        <FormContainer />
      </div>
    );
  }
}
//export default createForm(TripleGEO, 'triplegeo');
