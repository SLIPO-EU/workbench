import React from 'react';
import { SelectField, TextField } from '../../../helpers/forms/wizard-fields/';

const supportedFiles = [
  { value: 'SHAPEFILE' },
  { value: 'CSV' },
  { value: 'GPX' },
  { value: 'GEOJSON' },
  { value: 'OSM' },
  { value: 'RDF' },
];

export const initialValue = {
  name: '',
  description: '',
  format: null,
};

export const validator = (value) => {
  const errors = {};

  if (!value.name) {
    errors.name = 'File name required';
  }
  if (!value.format) {
    errors.format = 'Format required';
  }
  if (!value.description || value.description.length < 5) {
    errors.description = 'Description should be longer than 5 characters';
  }

  if (Object.keys(errors).length) {
    throw errors;
  }
};

export const Component = (props) => {
  return (
    <div>
      <TextField
        {...props}
        id="name"
        label="Resource name"
        help="Resource name"
      />
      <TextField
        {...props}
        id="description"
        label="Resource description"
        help="Resource description"
      />
      <SelectField
        {...props}
        id="format"
        label="Resource type"
        help="Upload file format"
        options={supportedFiles}
      /> 
    </div>
  );
};
