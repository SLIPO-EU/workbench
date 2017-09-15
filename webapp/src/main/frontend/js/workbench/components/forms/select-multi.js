import React from 'react';
import PropTypes from 'prop-types';
import ReactSelect from 'react-select';

import decorateField from './formfield';

export function MultiSelect(props) {
  return (
    <ReactSelect
      name={props.id} 
      id={props.id} 
      multi
      value={Array.isArray(props.value) ? props.value.map(v => ({ value: v, label: v })) : [{ value: props.value, label: props.value }]}
      onChange={(val) => {
        if (typeof props.onChange === 'function') {
          if (Array.isArray(val)) {
            props.onChange(val.map(v => v.value));
          } else {
            props.onChange(null);
          }
        }
      }}
      options={props.options.map(option => ({ value: option.value, label: option.label ? option.label : option.value }))}
    />
  );
}

export default decorateField(MultiSelect);


MultiSelect.propTypes = {
  id: PropTypes.string.isRequired,
  options: PropTypes.array.isRequired,
  value: PropTypes.any,
  onChange: PropTypes.func.isRequired,
};

