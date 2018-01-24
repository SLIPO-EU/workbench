import React from 'react';
import PropTypes from 'prop-types';
import { Input } from 'reactstrap';

import decorateField from './form-field';


export function TextArea(props) {
  return (
    <Input
      type="textarea"
      rows={props.rows || 2}
      name={props.id}
      id={props.id}
      state={props.state}
      value={props.value || ''}
      onChange={e => typeof props.onChange === 'function' ? props.onChange(e.target.value) : null}
      readOnly={props.readOnly}
      rows={props.rows || 1}
    />
  );
}

export default decorateField(TextArea);


TextArea.propTypes = {
  id: PropTypes.string.isRequired,
  label: PropTypes.string,
  help: PropTypes.string,
  value: PropTypes.string,
  state: PropTypes.oneOf(['success', 'warning', 'danger']),
  onChange: PropTypes.func,
  rows: PropTypes.number,
};
