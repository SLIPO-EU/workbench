import React from 'react';
import { ResourceSelectField } from '../../../helpers/forms/form-fields/';

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
      <ResourceSelectField
        {...props}
        id="resource"
        label="Catalog resource"
        help="Drag and drop a catalog resource"
        style={{ height: '40vh' }}
      />
    </div>
  );
};
