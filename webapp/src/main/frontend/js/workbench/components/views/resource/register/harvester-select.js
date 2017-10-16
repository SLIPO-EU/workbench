import React from 'react';
import { SelectField, TextField } from '../../../helpers/forms/wizard-fields/';
import validateUrl from '../../../../util/validate-url';

const harvesters = [
  { value: 'OSM', label: 'OSM Harvester' },
  { value: 'X', label: 'Harvester X' },
];

export const initialValue = {
  url: null,
};

export const validator = (value) => {
  const errors = {};
  if (!value.type) {
    errors.type = 'Type required';
  }
  return validateUrl(value.url)
    .then(() => { if (Object.keys(errors).length) { throw errors; }})
    .catch((err) => { errors.url = err; throw errors;});
};

export const Component = (props) => {
  return (
    <div>
      <TextField
        {...props}
        id="url"
        label="Harvester url"
        help="Enter a url"
      />
      <SelectField
        {...props}
        id="type"
        label="Harvester type"
        help="Choose harvester type to determine config options"
        options={harvesters}
      />
    </div>
  );
};
