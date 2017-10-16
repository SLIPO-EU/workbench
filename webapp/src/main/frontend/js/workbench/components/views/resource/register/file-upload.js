import React from 'react';
import { FileDropField, TextField } from '../../../helpers/forms/wizard-fields/';

export const initialValue = {
  file: null,
  name: null,
  description: null,
};

export const validator = (value) => {
  const errors = {};

  if (!value.file) {
    errors.file = 'File required';
  }
  if (!value.name) {
    errors.name = 'File name required';
  }
  if (Object.keys(errors).length) {
    throw errors;
  }
};

export class Component extends React.Component {
  constructor() {
    super();
  }
  componentWillReceiveProps(nextProps) {
    if (nextProps.value && nextProps.value.file && nextProps.value.file.name && !this.props.value.name) {
      this.props.setValue({ ...this.props.value, ...nextProps.value, name: nextProps.value.file.name });
    }
  }
  render() {
    return (
      <div>
        <FileDropField
          {...this.props}
          id="file"
          label="Upload file"
          help="Click to select or drop file"
        />
        <TextField
          {...this.props}
          id="name"
          label="Alias"
          help="File alias"
        />
      </div>
    );
  }
}
