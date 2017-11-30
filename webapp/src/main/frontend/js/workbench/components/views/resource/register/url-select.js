import React from 'react';
import { TextField } from '../../../helpers/forms/form-fields/';
import validateUrl from '../../../../util/validate-url';

export const initialValue = {
  url: null,
};

export const validator = (value) => validateUrl(value.url).catch((err) => { throw { url: err }; });

export const Component = (props) => {
  return (
    <div>
      <TextField
        {...props}
        id="url"
        label="Resource url"
        help="Enter an external url"
      />
    </div>
  );
};

