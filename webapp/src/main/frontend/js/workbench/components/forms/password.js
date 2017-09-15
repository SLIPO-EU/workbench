import React from 'react';
import PropTypes from 'prop-types';
import { Input } from 'reactstrap';

import decorateField from './formfield';


export function Password(props) {
  return (
    <Input 
      type="password" 
      name={props.id} 
      id={props.id} 
      state={props.state} 
      value={props.value || ''}
      onChange={e => typeof props.onChange === 'function' ? props.onChange(e.target.value) : null} 
    />
  );
}

export default decorateField(Password);


Password.propTypes = {
  id: PropTypes.string.isRequired,
  label: PropTypes.string,
  help: PropTypes.string,
  value: PropTypes.string,
  state: PropTypes.oneOf(['success', 'warning', 'danger']),
  onChange: PropTypes.func.isRequired,
};
