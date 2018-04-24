import React from 'react';

import {
  FileSelectField,
  SelectField,
  TextField,
} from '../../../helpers/forms/form-fields/';

const inputFormats = [
  { value: 'CSV', label: 'CSV' },
  { value: 'GPX', label: 'GPX' },
  { value: 'GEOJSON', label: 'GEOJSON' },
  { value: 'OSM', label: 'OSM' },
  { value: 'SHAPEFILE', label: 'SHAPEFILE' },
];

const modes = [
  { value: 'GRAPH', label: 'GRAPH' },
  { value: 'STREAM', label: 'STREAM' },
];

const encodings = [
  { value: 'ISO-8859-1', label: 'ISO-8859-1' },
  { value: 'ISO-8859-7', label: 'ISO-8859-7' },
  { value: 'UTF-8', label: 'UTF-8' },
];

const ontologies = [
  { value: 'GEOSPARQL', label: 'GeoSPARQL' },
  { value: 'VIRTUOSO', label: 'Virtuoso' },
  { value: 'WGS84', label: 'WGS84' },
];

const crs = [
  { value: 'EPSG:2100', label: 'EPSG:2100' },
  { value: 'EPSG:4326', label: 'EPSG:4326' },
];

const languages = [
  { value: 'en', label: 'English' },
  { value: 'el', label: 'Greek' },
];

const serializations = [
  { value: 'RDF_XML', label: 'RDF/XML' },
  { value: 'RDF_XML_ABBREV', label: 'RDF/XML-ABBREV' },
  { value: 'N_TRIPLES', label: 'N-TRIPLES' },
  { value: 'TURTLE', label: 'TURTLE' },
  { value: 'N3', label: 'N3' },
];

export const initialValue = {
  'mode': modes[0].value,
  'encoding': encodings[2].value,
  'delimiter': '|',
  'quote': '"',
  'serialization': serializations[2].value,
  'targetOntology': ontologies[0].value,
  'nsFeatureURI': 'http://slipo.eu/geodata#',
  'nsGeometryURI': 'http://www.opengis.net/ont/geosparql#',
  'sourceCRS': crs[1].value,
  'targetCRS': crs[1].value,
  'defaultLang': languages[0].value,
};

export { validator } from '../../../../service/triplegeo';

export const Component = (props) => {
  const { errors, readOnly, setValue, value, appConfiguration, ...rest } = props;

  const inject = {
    errors,
    readOnly,
    setValue,
    value,
  };

  return (
    <div>
      < div >
        <h4>Input parameters</h4>
        <hr />
      </div>

      <SelectField
        {...inject}
        id="inputFormat"
        label="Input format"
        help="Specify format for the input geographical file(s): "
        options={inputFormats}
      />

      <SelectField
        {...inject}
        id="mode"
        label="Mode"
        help="Conversion mode"
        options={modes}
      />

      <SelectField
        {...inject}
        id="encoding"
        label="Encoding"
        help="The encoding (character set) for strings in the input data. If not specified, UTF-8 encoding is assumed."
        options={encodings}
      />

      {/* <div>
        <h4>Output parameters</h4>
        <hr />
      </div>

      <SelectField
        {...inject}
        id="serialization"
        label="Serialization format"
        help="Specify export serialization for the output file"
        options={serializations}
      />

      <SelectField
        {...inject}
        id="targetOntology"
        label="Ontology Type"
        help="Specify the type of the spatial ontology where the exported data will refer to"
        options={ontologies}
      /> */}

      <div>
        <h4>Data parameters</h4>
        <hr />
      </div>

      <TextField
        {...inject}
        id="attrKey"
        label="Attribute key"
        help="Field name containing unique identifier for each entity (i.e., each record in the shapefile)"
      />

      <TextField
        {...inject}
        id="attrName"
        label="Attribute name"
        help="Field name from which name literals (i.e., strings) will be extracted"
      />

      <TextField
        {...inject}
        id="attrCategory"
        label="Attribute category"
        help="Field name from which classification literals (e.g., type of points, road classes etc.) will be extracted. Set value UNK if non applicable"
      />

      {props.value && props.value.inputFormat === 'SHAPEFILE' &&
        <TextField
          {...inject}
          id="attrGeometry"
          label="Attribute geometry"
          help="The name of the geometry column in the input dataset"
        />
      }

      <TextField
        {...inject}
        id="valIgnore"
        label="Ignore value"
        help="Parameter that specifies particular values (e.g., UNK) in attributes that should not be exported as literals. By default, NULL values in attributes are suppressed and never exported"
      />
      {props.value && props.value.inputFormat === 'CSV' &&
        <div>
          <TextField
            {...inject}
            id="delimiter"
            label="Delimiter"
            help="Specify delimiter character"
          />

          <TextField
            {...inject}
            id="quote"
            label="Quote"
            help="Specify quote character for string values; Remove for any other types of input data"
          />

          <TextField
            {...inject}
            id="attrX"
            label="X-attribute"
            help="Specify attribute holding X-coordinates of point locations"
          />

          <TextField
            {...inject}
            id="attrY"
            label="Y-attribute"
            help="Specify attribute holding Y-coordinates of point locations"
          />
        </div>
      }

      <div>
        <h4>Namespace parameters</h4>
        <hr />
      </div>

      <TextField
        {...inject}
        id="featureName"
        label="Feature name"
        help="Parameter that specifies a user-defined name for the resources that will be created"
      />

      <TextField
        {...inject}
        id="nsFeatureURI"
        label="Feature URI"
        help="Specify the common URI namespace for all generated resources"
      />

      <TextField
        {...inject}
        id="prefixFeatureNS"
        label="Feature Prefix"
        help="Define a prefix name for the utilized URI namespace (i.e., the previously declared with nsFeatureURI)"
      />

      <TextField
        {...inject}
        id="nsGeometryURI"
        label="Geometry URI"
        help="Specify the namespace for the underlying geospatial ontology"
      />

      <TextField
        {...inject}
        id="prefixGeometryNS"
        label="Geometry Prefix"
        help="Define a prefix name for the geospatial ontology (i.e., the previously declared with nsGeometryURI)"
      />


      <div>
        <h4>Spatial Reference parameters</h4>
        <hr />
      </div>

      <SelectField
        {...inject}
        id="sourceCRS"
        label="Source CRS"
        help=""
        options={crs}
      />

      <SelectField
        {...inject}
        id="targetCRS"
        label="Target CRS"
        help=""
        options={crs}
      />

      {props.value && (props.value.mode === 'GRAPH' || props.value.mode === 'STREAM') &&
        <div>
          <div>
            <h4>Other parameters</h4>
            <hr />
          </div>

          <SelectField
            {...inject}
            id="defaultLang"
            label="Default language"
            help="Default lang for the labels created in the output RDF. By default, the value will be English-en"
            options={languages}
          />
        </div>
      }
    </div>
  );
};
