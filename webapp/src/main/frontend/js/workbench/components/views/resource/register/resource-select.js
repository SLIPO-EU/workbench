import React from 'react';
import { SelectField } from '../../../helpers/forms/wizard-fields/';

const availableResources = [
  { value: 'Resource 1', },
  { value: 'Resource 2', },
  { value: 'Resource 3', },
];

export const initialValue = {
  resource: null,
};
export const validator = (value) => {
  const errors = {};

  if (!value.resource) {
    errors.resource = 'Select a resource';
  }
  if (Object.keys(errors).length) {
    throw errors;
  }
};

export const Component = (props) => {
  return (
    <div>
      <SelectField
        {...props}
        id="resource"
        label="Resource"
        help="Select resource from list"
        options={availableResources}
      />
    </div>
  );
};
