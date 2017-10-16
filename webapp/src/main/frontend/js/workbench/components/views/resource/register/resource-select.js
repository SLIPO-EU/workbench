import React from 'react';
import { MultiSelectField } from '../../../helpers/forms/wizard-fields/';

const availableResources = [
  { value: 'Resource 1', },
  { value: 'Resource 2', },
  { value: 'Resource 3', },
];

export const initialValue = {
  resources: [],
};
export const validator = (value) => {
  const errors = {};

  if (!value.resources || (value.resources && !value.resources.length)) {
    errors.resources = 'Select at least 1 resource';
  }
  if (Object.keys(errors).length) {
    throw errors;
  }
};

export const Component = (props) => {
  return (
    <div>
      <MultiSelectField
        {...props}
        id="resources"
        label="Resources"
        help="Select resources from list"
        options={availableResources}
      />
    </div>
  );
};
