import React from 'react';
import PropTypes from 'prop-types';
import ReactSelect from 'react-select';

import decorateField from './formfield';

export function MultiSelect(props) {
  const options = props.options.map(option => ({ value: option.value, label: option.label || option.value }));
  return (
    <ReactSelect
      name={props.id} 
      id={props.id} 
      multi
      value={props.value.map(val => options.find(opt => opt.value === val))}
      onChange={(val) => {
        if (typeof props.onChange === 'function') {
          if (Array.isArray(val)) {
            props.onChange(val.map(v => v.value));
          } else {
            props.onChange(null);
          }
        }
      }}
      options={options}
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

