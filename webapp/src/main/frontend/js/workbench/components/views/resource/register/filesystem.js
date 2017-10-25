import React from 'react';
import { FileSelectField } from '../../../helpers/forms/wizard-fields/';

export const initialValue = {
  resource: null,
};
export const validator = (value) => {
  const errors = {};

  if (!value.resource) {
    errors.resource = 'No resource selected';
  }
  if (Object.keys(errors).length) {
    throw errors;
  }
};

export const Component = (props) => {
  return (
    <div>
      <FileSelectField
        {...props}
        id="resource"
        label="File system resource"
        help="Click on resource to select"
      />
    </div>
  );
};
