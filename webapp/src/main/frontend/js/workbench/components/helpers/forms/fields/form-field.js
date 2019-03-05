import React from 'react';
import PropTypes from 'prop-types';
import { FormGroup, FormText, Label, FormFeedback } from 'reactstrap';


/**
 * A functional component for extending form fields with label, feedback and description
 * elements
 *
 * @export
 * @param {object} props - The properties to pass to the wrapped component
 * @returns The new component
 */
export function FormField(props) {
  return (
    <FormGroup color={props.error ? 'danger' : null}>
      {props.label && props.showLabel &&
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
  showLabel: PropTypes.bool,
};

FormField.defaultProps = {
  showLabel: true,
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

