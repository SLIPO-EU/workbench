import React from 'react';
import { TextField } from '../../../helpers/forms/wizard-fields/';

export const initialValue = {};

export const validator = () => {};

export const Component = (props) => {
  const { values } = props;
  const { type } = values.harvester;
  if (type.value === 'OSM') {
    return (
      <div>
        <TextField
          {...props}
          id="option1"
          label="OSM option1"
          help=""
        />
        <TextField
          {...props}
          id="option2"
          label="OSM option2"
          help=""
        />
      </div>
    );
  } else if (type.value === 'X') {
    return (
      <div>
        <TextField
          {...props}
          id="option1"
          label="X option1"
          help=""
        />
        <TextField
          {...props}
          id="option2"
          label="X option2"
          help=""
        />
      </div>
    );
  }
  return null;
};
