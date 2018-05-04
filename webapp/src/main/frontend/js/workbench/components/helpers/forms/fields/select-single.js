import React from 'react';
import PropTypes from 'prop-types';
import ReactSelect from 'react-select';

import decorateField from './form-field';

export function Select(props) {
  const options = props.options.map(option => ({ value: option.value, label: option.label || option.value }));
  return (
    <ReactSelect
      name={props.id}
      id={props.id}
      value={options.find(opt => opt.value === props.value)}
      clearable={props.clearable || false}
      onChange={(val) => {
        if (typeof props.onChange === 'function') {
          if (val && val.value) {
            props.onChange(val.value);
          } else {
            props.onChange(null);
          }
        }
      }}
      options={options}
      disabled={props.readOnly}
    />
  );
}

export default decorateField(Select);


Select.propTypes = {
  id: PropTypes.string.isRequired,
  options: PropTypes.array.isRequired,
  value: PropTypes.any,
  onChange: PropTypes.func.isRequired,
  readOnly: PropTypes.bool,
};

