import React from 'react';
import PropTypes from 'prop-types';
import ReactSelect from 'react-select';

import decorateField from './form-field';

import { styles } from './select-shared';

/**
 * A functional component for rendering a drop down list that supports multiple
 * items selection. The component is based on the ReactSelect component
 *
 * @export
 * @param {object} props - Component properties
 * @returns The new component
 */
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
      disabled={props.readOnly}
      styles={styles}
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

