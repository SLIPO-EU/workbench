import React from 'react';
import PropTypes from 'prop-types';
import ReactSelect from 'react-select';

import decorateField from './formfield';

export function Select(props) {
  return (
    <ReactSelect
      name={props.id} 
      id={props.id} 
      value={{ value: props.value, label: props.value }}
      clearable={false}
      onChange={(val) => {
        if (typeof props.onChange === 'function') {
          if (val && val.value) {
            props.onChange(val.value);
          } else {
            props.onChange(null);
          }
        }
      }} 
      options={props.options.map(option => ({ value: option.value, label: option.label ? option.label : option.value }))}
    />
  );
}

export default decorateField(Select);


Select.propTypes = {
  id: PropTypes.string.isRequired,
  options: PropTypes.array.isRequired,
  value: PropTypes.any,
  onChange: PropTypes.func.isRequired,
};

