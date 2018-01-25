import React from 'react';
import { TextField, TextAreaField } from '../../../helpers/forms/form-fields/';

export const validator = (value) => {
  const errors = {};

  if (!value.name) {
    errors.name = 'Name is required';
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
        label="Name"
        help="A user friendly name"
      />
      <TextAreaField
        {...props}
        rows={5}
        id="description"
        label="Description"
        help="A short description of the data integration process"
      />
    </div>
  );
};
