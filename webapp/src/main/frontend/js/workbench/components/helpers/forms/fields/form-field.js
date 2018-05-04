import React from 'react';
import PropTypes from 'prop-types';
import { FormGroup, FormText, Label, FormFeedback } from 'reactstrap';


export function FormField(props) {
  return (
    <FormGroup color={props.error ? 'danger' : null}>
      {props.label &&
        <Label for={props.id}>{props.label}</Label>
      }
      {props.children}
      {
        props.error ?
          <FormFeedback>{props.error}</FormFeedback>
          : null
      }
      <FormText color="muted">{props.help}</FormText>
    </FormGroup>

  );
}

FormField.propTypes = {
  id: PropTypes.string.isRequired,
  label: PropTypes.string,
  help: PropTypes.string,
  error: PropTypes.string,
};


export default function decorateField(Component) {
  return function (props) {
    return (
      <FormField
        {...props}
      >
        <Component {...props} />
      </FormField>
    );
  };
}

