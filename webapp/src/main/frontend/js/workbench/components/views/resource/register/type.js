import React from 'react';
import { SelectField } from '../../../helpers/forms/form-fields/';

const options = [
  { value: 'UPLOAD', label: 'Upload' },
  { value: 'FILESYSTEM', label: 'File system' },
  { value: 'EXTERNAL', label: 'External url' },
  { value: 'HARVESTER', label: 'Harvester' },
];

export const initialValue = {
  path: options[0].value,
};

export const validator = (value) => {
  if (value.path === 'EXTERNAL' || value.path === 'HARVESTER') {
    throw { path: 'External url & harvester not yet supported!' };
  }
};

export const Component = (props) => {
  return (
    <div>
      <SelectField
        {...props}
        id="path"
        label="Data source"
        help="Choose one"
        options={options}
      />
    </div>
  );
};
