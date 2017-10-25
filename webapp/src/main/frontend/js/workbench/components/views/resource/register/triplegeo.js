import React from 'react';
import { TextField, SelectField } from '../../../helpers/forms/wizard-fields/';


const serializations = [
  { value: 'RDF/XML', label: 'RDF/XML' },
  { value: 'RDF/XML-ABBREV', label: 'RDF/XML-ABBREV' },
  { value: 'N-TRIPLES', label: 'N-TRIPLES' },
  { value: 'TURTLE', label: 'TURTLE' },
  { value: 'N3', label: 'N3' },
];

const ontologies = [
  { value: 'GeoSPARQL', label: 'GeoSPARQL' },
  { value: 'Virtuoso', label: 'Virtuoso' },
  { value: 'WGS84', label: 'WGS84' },
];

const crs = [
  { value: 'EPSG:2100', label: 'EPSG:2100' },
  { value: 'EPSG:4326', label: 'EPSG:4326' },
];

const languages = [
  { value: 'en', label: 'English' },
  { value: 'en', label: 'Greek' },
];

export const initialValue = {
  'serialization': serializations[2].value,
  'targetOntology': ontologies[1].value,
  'sourceCRS': crs[0].value,
  'targetCRS': crs[1].value,
  'defaultLang': languages[0].value,
};

export const validator = function (values, cleared) {
  const errors = {};
  if (!values['attrKey']) {
    errors['attrKey'] = 'Required';
  }
  if (!values['featureName']) {
    errors['featureName'] = 'Required for constructing the resource URI';
  }
  if (!values['targetOntology']) {
    errors['targetOntology'] = 'Required';
  }
  if (!values['defaultLang']) {
    errors['defaultLang'] = 'Select default language';
  }
  if (cleared && cleared.metadata && cleared.metadata.format === 'CSV') {
    if (!values['delimiter']) {
      errors['delimiter'] = 'Required for CSV';
    } 
    if (!values['attrX']) {
      errors['attrX'] = 'Required for CSV';
    }
    if (!values['attrY']) {
      errors['attrY'] = 'Required for CSV';
    }
  }
  
  if (Object.keys(errors).length) {
    throw errors;
  }
};

export const Component = (props) => {
  return (
    <div>
      <div>
        <h4>Output parameters</h4>
        <hr />
      </div>

      <SelectField
        {...props}
        id="serialization"
        label="Serialization format"
        help="Specify export serialization for the output file"
        options={serializations}
      />

      <SelectField
        {...props}
        id="targetOntology"
        label="Ontology Type"
        help="Specify the type of the spatial ontology where the exported data will refer to"
        options={ontologies}
      />

      <div>
        <h4>Data parameters</h4>
        <hr />
      </div>
      
      <TextField
        {...props}
        id="attrKey"
        label="Attribute key"
        help="Field name containing unique identifier for each entity (i.e., each record in the shapefile)"
      />
      
      <TextField
        {...props}
        id="attrName"
        label="Attribute name"
        help="Field name from which name literals (i.e., strings) will be extracted"
      />

      <TextField
        {...props}
        id="attrCategory"
        label="Attribute category"
        help="Field name from which classification literals (e.g., type of points, road classes etc.) will be extracted. Set value UNK if non applicable"
      />
      
      <TextField
        {...props}
        id="valIgnore"
        label="Ignore value"
        help="Parameter that specifies particular values (e.g., UNK) in attributes that should not be exported as literals. By default, NULL values in attributes are suppressed and never exported"
      />
      { 
        props.values.metadata && props.values.metadata.format === 'CSV' ?
          <div>
            <TextField
              {...props}
              id="delimiter"
              label="Delimiter"
              help="Specify delimiter character"
            />
            
            <TextField
              {...props}
              id="attrX"
              label="X-attribute"
              help="Specify attribute holding X-coordinates of point locations"
            />

            <TextField
              {...props}
              id="attrY"
              label="Y-attribute"
              help="Specify attribute holding Y-coordinates of point locations"
            />
          </div>
          : null
      }
      
      <div>
        <h4>Namespace parameters</h4>
        <hr />
      </div>

      <TextField
        {...props}
        id="featureName"
        label="Feature name"
        help="Parameter that specifies a user-defined name for the resources that will be created"
      />

      <TextField
        {...props}
        id="nsFeatureURI"
        label="Feature URI"
        help="Specify the common URI namespace for all generated resources"
      />

      <TextField
        {...props}
        id="prefixFeatureNS"
        label="Feature Prefix"
        help="Define a prefix name for the utilized URI namespace (i.e., the previously declared with nsFeatureURI)"
      />

      <TextField
        {...props}
        id="nsGeometryURI"
        label="Geometry URI"
        help="Specify the namespace for the underlying geospatial ontology"
      />

      <TextField
        {...props}
        id="prefixGeometryNS"
        label="Geometry Prefix"
        help="Define a prefix name for the geospatial ontology (i.e., the previously declared with nsGeometryURI)"
      />


      <div>
        <h4>Spatial Reference parameters</h4>
        <hr />
      </div>

      <SelectField
        {...props}
        id="sourceCRS"
        label="Source CRS"
        help=""
        options={crs}
      />

      <SelectField
        {...props}
        id="targetCRS"
        label="Target CRS"
        help=""
        options={crs}
      />
      
      <div>
        <h4>Other parameters</h4>
        <hr />
      </div>
      
      <SelectField
        {...props}
        id="defaultLang"
        label="Default language"
        help="Default lang for the labels created in the output RDF. By default, the value will be English-en"
        options={languages}
      />

    </div>
  );
};
