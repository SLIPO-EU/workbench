import React from 'react';
import { FileSelectField } from '../../../helpers/forms/wizard-fields/';
import formatFileSize from '../../../../util/file-size';

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
        data={props.resources}
      />
    </div>
  );
};
